package com.pramuditha.batterynotifier

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "PowerConnectionReceiver Triggered!", Toast.LENGTH_LONG).show()
        if (context == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, BatteryCheckReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Get the SharedPreferences file for the app's state
        val statePrefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
        Log.d(intent?.action, "INTENT_ACTION")
        if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
            Log.d("PowerReceiver", "Charger connected! Scheduling repeating alarm.")

            // Schedule the alarm
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                10 * 60 * 1000L, // 10 minutes
                pendingIntent
            )
            // NEW: Save the state
            statePrefs.edit().putBoolean("SERVICE_STATE", true).apply()

        } else if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
            Log.d("PowerReceiver", "Charger disconnected! Canceling alarm.")
            // Cancel the alarm
            alarmManager.cancel(pendingIntent)
            // NEW: Save the state
            statePrefs.edit().putBoolean("SERVICE_STATE", false).apply()
        }
    }
}