package com.example.service

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.data.db.AppDatabase
import com.example.data.model.DestinationType
import com.example.data.model.ForwardLog
import com.example.data.model.ForwardRule
import com.example.network.SmtpMailer
import com.example.network.WebhookSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ForwardingManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    private val webhookSender: WebhookSender = WebhookSender(),
    private val smtpMailer: SmtpMailer = SmtpMailer()
) {

    data class ForwardExecutionResult(
        val rule: ForwardRule,
        val destinationType: DestinationType,
        val destination: String,
        val isSuccess: Boolean,
        val message: String
    )

    suspend fun processIncomingSms(
        sender: String,
        body: String,
        timestamp: Long = System.currentTimeMillis()
    ): List<ForwardExecutionResult> = withContext(Dispatchers.IO) {
        val ruleDao = database.ruleDao()
        val logDao = database.forwardLogDao()
        val activeRules = ruleDao.getActiveRules()

        val results = mutableListOf<ForwardExecutionResult>()

        for (rule in activeRules) {
            if (RuleMatcher.matches(rule, sender, body)) {
                val execResult = executeForward(rule, sender, body, timestamp)
                results.add(execResult)

                // Log result to database
                val log = ForwardLog(
                    ruleId = rule.id,
                    ruleName = rule.name,
                    sender = sender,
                    smsBody = body,
                    destinationType = execResult.destinationType,
                    destination = execResult.destination,
                    isSuccess = execResult.isSuccess,
                    responseStatus = execResult.message,
                    timestamp = timestamp
                )
                logDao.insertLog(log)

                // If success, update rule stats
                if (execResult.isSuccess) {
                    ruleDao.incrementRuleMatchCount(rule.id, timestamp)
                }
            }
        }

        if (results.isNotEmpty()) {
            notifyForwardActivity(sender, results)
        }

        results
    }

    private suspend fun executeForward(
        rule: ForwardRule,
        sender: String,
        body: String,
        timestamp: Long
    ): ForwardExecutionResult {
        return when (rule.destinationType) {
            DestinationType.WEBHOOK -> {
                val destination = rule.webhookUrl
                val res = webhookSender.sendWebhook(
                    url = rule.webhookUrl,
                    method = rule.webhookMethod,
                    headersText = rule.webhookHeaders,
                    sender = sender,
                    message = body,
                    timestamp = timestamp,
                    ruleName = rule.name
                )
                if (res.isSuccess) {
                    ForwardExecutionResult(
                        rule = rule,
                        destinationType = DestinationType.WEBHOOK,
                        destination = destination,
                        isSuccess = true,
                        message = res.getOrNull() ?: "Success"
                    )
                } else {
                    ForwardExecutionResult(
                        rule = rule,
                        destinationType = DestinationType.WEBHOOK,
                        destination = destination,
                        isSuccess = false,
                        message = res.exceptionOrNull()?.message ?: "Webhook call failed"
                    )
                }
            }
            DestinationType.EMAIL -> {
                val destination = rule.recipientEmail
                val smtpConfig = database.smtpConfigDao().getSmtpConfig()
                if (smtpConfig == null || smtpConfig.host.isBlank()) {
                    return ForwardExecutionResult(
                        rule = rule,
                        destinationType = DestinationType.EMAIL,
                        destination = destination,
                        isSuccess = false,
                        message = "SMTP configuration is missing. Please configure SMTP settings."
                    )
                }

                val subject = formatEmailSubject(rule.emailSubject, sender, rule.name)
                val emailBody = formatEmailHtmlBody(sender, body, timestamp, rule.name)

                val smtpRes = smtpMailer.sendEmail(
                    config = smtpConfig,
                    recipientEmail = rule.recipientEmail,
                    subject = subject,
                    body = emailBody,
                    isHtml = true
                )

                ForwardExecutionResult(
                    rule = rule,
                    destinationType = DestinationType.EMAIL,
                    destination = destination,
                    isSuccess = smtpRes.success,
                    message = smtpRes.message
                )
            }
        }
    }

    private fun formatEmailSubject(template: String, sender: String, ruleName: String): String {
        var result = template.ifBlank { "SMS from {sender}" }
        result = result.replace("{sender}", sender)
        result = result.replace("{rule}", ruleName)
        return result
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun formatEmailHtmlBody(sender: String, body: String, timestamp: Long, ruleName: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(timestamp))
        val escapedBody = escapeHtml(body)
        val escapedSender = escapeHtml(sender)
        val escapedRule = escapeHtml(ruleName)

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                        color: #1a1a1a;
                        margin: 0;
                        padding: 16px;
                        background-color: #ffffff;
                    }
                    .sms-content {
                        font-size: 16px;
                        line-height: 1.6;
                        color: #111827;
                        white-space: pre-wrap;
                        word-break: break-word;
                        margin: 0 0 16px 0;
                    }
                    .metadata-section {
                        margin-top: 16px;
                        padding-top: 14px;
                        border-top: 1px solid #e5e7eb;
                        font-size: 13px;
                        color: #6b7280;
                        line-height: 1.5;
                    }
                    .meta-row {
                        margin-bottom: 4px;
                    }
                    .meta-label {
                        font-weight: 600;
                        color: #4b5563;
                    }
                    .footer-tag {
                        margin-top: 10px;
                        font-size: 11px;
                        color: #9ca3af;
                    }
                </style>
            </head>
            <body>
                <div class="sms-content">$escapedBody</div>
                <br><br>
                <div class="metadata-section">
                    <div class="meta-row"><span class="meta-label">Sender:</span> $escapedSender</div>
                    <div class="meta-row"><span class="meta-label">Date:</span> $formattedDate</div>
                    <div class="meta-row"><span class="meta-label">Matched Rule:</span> $escapedRule</div>
                    <div class="footer-tag">Forwarded automatically by SMS Forwarder for Android</div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun notifyForwardActivity(sender: String, results: List<ForwardExecutionResult>) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val successCount = results.count { it.isSuccess }
            val failCount = results.count { !it.isSuccess }

            val title = if (failCount == 0) {
                "SMS Forwarded Successfully"
            } else {
                "SMS Forward: $successCount succeeded, $failCount failed"
            }

            val content = "From: $sender (${results.size} rule${if (results.size > 1) "s" else ""} triggered)"

            val notification = NotificationCompat.Builder(context, SmsForwarderService.NOTIFICATION_CHANNEL_ACTIVITY)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        } catch (_: Exception) {
            // Notification dispatch may fail if permission denied
        }
    }
}
