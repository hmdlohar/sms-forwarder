package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.service.ForwardingManager
import com.example.service.SmsForwarderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        if (!SmsForwarderService.isServiceRunning(context)) {
            Log.d("SmsReceiver", "SMS Forwarder service is disabled by user. Skipping.")
            return
        }

        val messages: Array<SmsMessage> = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: emptyArray()
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Failed to extract SMS from intent", e)
            emptyArray()
        }

        if (messages.isEmpty()) {
            return
        }

        // Group messages by originating address
        val messagesBySender = mutableMapOf<String, StringBuilder>()
        var timestamp = System.currentTimeMillis()

        for (sms in messages) {
            val sender = sms.displayOriginatingAddress ?: sms.originatingAddress ?: "Unknown"
            val body = sms.displayMessageBody ?: sms.messageBody ?: ""
            timestamp = sms.timestampMillis

            val currentBuilder = messagesBySender.getOrPut(sender) { StringBuilder() }
            currentBuilder.append(body)
        }

        // Process forwarding asynchronously
        val pendingResult = goAsync()
        val forwardingManager = ForwardingManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                for ((sender, bodyBuilder) in messagesBySender) {
                    val fullBody = bodyBuilder.toString()
                    Log.d("SmsReceiver", "Processing incoming SMS from $sender: $fullBody")
                    forwardingManager.processIncomingSms(
                        sender = sender,
                        body = fullBody,
                        timestamp = timestamp
                    )
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error while processing incoming SMS forward", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
