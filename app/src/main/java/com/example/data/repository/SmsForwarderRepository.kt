package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.ForwardLog
import com.example.data.model.ForwardRule
import com.example.data.model.SmtpConfig
import com.example.network.SmtpMailer
import com.example.network.WebhookSender
import com.example.service.ForwardingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SmsForwarderRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val ruleDao = database.ruleDao()
    private val smtpConfigDao = database.smtpConfigDao()
    private val forwardLogDao = database.forwardLogDao()

    private val webhookSender = WebhookSender()
    private val smtpMailer = SmtpMailer()
    private val forwardingManager = ForwardingManager(context, database, webhookSender, smtpMailer)

    val allRules: Flow<List<ForwardRule>> = ruleDao.getAllRules()
    val smtpConfig: Flow<SmtpConfig?> = smtpConfigDao.getSmtpConfigFlow()
    val allLogs: Flow<List<ForwardLog>> = forwardLogDao.getAllLogs()
    val totalLogsCount: Flow<Int> = forwardLogDao.getTotalLogsCount()
    val successLogsCount: Flow<Int> = forwardLogDao.getSuccessLogsCount()

    suspend fun saveRule(rule: ForwardRule): Long = withContext(Dispatchers.IO) {
        if (rule.id == 0L) {
            ruleDao.insertRule(rule)
        } else {
            ruleDao.updateRule(rule)
            rule.id
        }
    }

    suspend fun setRuleEnabled(ruleId: Long, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        ruleDao.setRuleEnabled(ruleId, isEnabled)
    }

    suspend fun deleteRule(rule: ForwardRule) = withContext(Dispatchers.IO) {
        ruleDao.deleteRule(rule)
    }

    suspend fun saveSmtpConfig(config: SmtpConfig) = withContext(Dispatchers.IO) {
        smtpConfigDao.saveSmtpConfig(config)
    }

    suspend fun getSmtpConfigDirect(): SmtpConfig? = withContext(Dispatchers.IO) {
        smtpConfigDao.getSmtpConfig()
    }

    suspend fun clearAllLogs() = withContext(Dispatchers.IO) {
        forwardLogDao.clearAllLogs()
    }

    suspend fun testWebhook(
        url: String,
        method: String,
        headers: String
    ): Result<String> {
        return webhookSender.testEndpoint(url, method, headers)
    }

    suspend fun testSmtp(
        config: SmtpConfig,
        recipient: String
    ): SmtpMailer.SmtpResult {
        return smtpMailer.sendEmail(
            config = config,
            recipientEmail = recipient,
            subject = "Test from SMS Forwarder",
            body = "This is a test email sent from SMS Forwarder to verify your SMTP connection settings."
        )
    }

    suspend fun simulateSms(
        sender: String,
        body: String
    ): List<ForwardingManager.ForwardExecutionResult> {
        return forwardingManager.processIncomingSms(
            sender = sender,
            body = body,
            timestamp = System.currentTimeMillis()
        )
    }

    private val inboxReader = SmsInboxReader(context)

    suspend fun loadDeviceInbox(limit: Int = 50): List<com.example.data.model.DeviceSms> {
        return inboxReader.readRecentInboxSms(limit)
    }

    suspend fun forwardSpecificSms(
        sender: String,
        body: String,
        timestamp: Long
    ): List<ForwardingManager.ForwardExecutionResult> {
        return forwardingManager.processIncomingSms(
            sender = sender,
            body = body,
            timestamp = timestamp
        )
    }
}
