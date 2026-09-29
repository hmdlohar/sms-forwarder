package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class SmsForwarderService : Service() {

    companion object {
        const val NOTIFICATION_CHANNEL_SERVICE = "sms_forwarder_service_channel"
        const val NOTIFICATION_CHANNEL_ACTIVITY = "sms_forwarder_activity_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val PREFS_NAME = "sms_forwarder_prefs"
        const val KEY_SERVICE_RUNNING = "is_service_running"

        fun isServiceRunning(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_SERVICE_RUNNING, true)
        }

        fun setServiceRunning(context: Context, isRunning: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SERVICE_RUNNING, isRunning).apply()
        }

        fun startService(context: Context) {
            setServiceRunning(context, true)
            val intent = Intent(context, SmsForwarderService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            setServiceRunning(context, false)
            val intent = Intent(context, SmsForwarderService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            setServiceRunning(this, false)
            return START_NOT_STICKY
        }

        val notification = createServiceNotification()
        startForeground(NOTIFICATION_ID, notification)
        setServiceRunning(this, true)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createServiceNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SERVICE)
            .setContentTitle("SMS Forwarder Active")
            .setContentText("Listening for incoming SMS to forward in background")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_SERVICE,
                "Background Forwarder Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent status while SMS Forwarder is active in background"
            }

            val activityChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ACTIVITY,
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
