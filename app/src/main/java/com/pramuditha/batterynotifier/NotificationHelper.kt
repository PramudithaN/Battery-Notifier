package com.pramuditha.batterynotifier

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    const val CHANNEL_MONITORING_ID = "battery_monitoring_channel"
    const val CHANNEL_ALERT_ID = "battery_alert_channel"

    const val NOTIFICATION_ID_FOREGROUND = 1001
    const val NOTIFICATION_ID_ALERT = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Silent ongoing channel for Foreground Service
            val monitoringChannel = NotificationChannel(
                CHANNEL_MONITORING_ID,
                "Battery Monitoring Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows continuous battery status while monitoring is active."
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(monitoringChannel)

            // 2. High-importance channel for Target Reached Alerts
            val alertSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val alertChannel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "Battery Target Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts you when the battery reaches your configured charge target."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                setSound(alertSoundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    fun buildForegroundNotification(
        context: Context,
        batteryPct: Int,
        targetPct: Int,
        isCharging: Boolean
    ): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, BatteryService::class.java).apply {
            action = BatteryService.ACTION_STOP_MONITORING
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val chargeStatusText = if (isCharging) "Charging" else "Not Charging"
        val contentText = "Battery: $batteryPct% ($chargeStatusText) • Target: $targetPct%"

        return NotificationCompat.Builder(context, CHANNEL_MONITORING_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("Battery Monitoring Active")
            .setContentText(contentText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Monitoring", stopPendingIntent)
            .build()
    }

    fun sendTargetReachedNotification(context: Context, batteryPct: Int, targetPct: Int) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            2,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, BatteryService::class.java).apply {
            action = BatteryService.ACTION_STOP_MONITORING
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            3,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("Battery Target Reached!")
            .setContentText("Battery has reached $batteryPct% (Target: $targetPct%). Unplug your charger to protect battery health.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(alertSoundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss & Stop", stopPendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION_ID_ALERT, builder.build())
    }

    fun cancelAlertNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_ALERT)
    }
}
