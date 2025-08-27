package com.pramuditha.batterynotifier

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

class BatteryCheckReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        // Get a sticky broadcast to read the current battery level without a running service
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            context.registerReceiver(null, iFilter)
        }

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()

        val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notificationLevel = sharedPrefs.getInt("NOTIFICATION_LEVEL", 80)

        Log.d("BatteryCheckReceiver", "Alarm triggered. Current: $batteryPct%, Target: $notificationLevel%")

        // If the target is reached, send the notification and cancel the alarm
        if (batteryPct >= notificationLevel) {
            Log.d("BatteryCheckReceiver", "Target reached! Sending notification and stopping alarm.")

            // Ensure notification channel exists
            createNotificationChannel(context)
            // Send the full-screen notification
            sendTargetReachedNotification(context)

            // Cancel the repeating alarm
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, BatteryCheckReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}