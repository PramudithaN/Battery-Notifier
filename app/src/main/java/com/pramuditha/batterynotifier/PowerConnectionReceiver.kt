package com.pramuditha.batterynotifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class PowerConnectionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PowerReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action == null) return

        val statePrefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
        val isServiceActive = statePrefs.getBoolean("SERVICE_STATE", false)

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                Log.d(TAG, "Charger connected")
                // When plugged in and monitoring was set to active, ensure service is running
                if (isServiceActive) {
                    val serviceIntent = Intent(context, BatteryService::class.java).apply {
                        action = BatteryService.ACTION_START_MONITORING
                    }
                    try {
                        ContextCompat.startForegroundService(context, serviceIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not launch foreground service from background broadcast: ${e.message}")
                    }
                }
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                Log.d(TAG, "Charger disconnected")
                // When unplugged, dismiss target alerts and stop monitoring
                NotificationHelper.cancelAlertNotification(context)

                if (isServiceActive) {
                    val stopIntent = Intent(context, BatteryService::class.java).apply {
                        action = BatteryService.ACTION_STOP_MONITORING
                    }
                    try {
                        context.startService(stopIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not deliver stop intent to service: ${e.message}")
                    }
                }
            }
        }
    }
}
