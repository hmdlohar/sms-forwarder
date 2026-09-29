package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.db.AppDatabase
import com.example.service.SmsForwarderService

class SmsForwarderApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // If enabled, start background service
        if (SmsForwarderService.isServiceRunning(this)) {
            try {
                SmsForwarderService.startService(this)
            } catch (_: Exception) {
                // Background start restriction may apply until user grants notification permission
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val serviceChannel = NotificationChannel(
                SmsForwarderService.NOTIFICATION_CHANNEL_SERVICE,
                "Background Forwarder Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent status while SMS Forwarder is active in background"
            }

            val activityChannel = NotificationChannel(
                SmsForwarderService.NOTIFICATION_CHANNEL_ACTIVITY,
                "Forwarding Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when SMS messages are forwarded"
            }

            notificationManager?.createNotificationChannel(serviceChannel)
            notificationManager?.createNotificationChannel(activityChannel)
        }
    }
}
