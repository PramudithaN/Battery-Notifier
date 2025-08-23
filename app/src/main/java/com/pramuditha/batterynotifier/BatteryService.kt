package com.pramuditha.batterynotifier // Make sure this matches your package name!

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import android.util.Log

class BatteryService : Service() {

    // INSIDE BatteryService.kt

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()

            // It already reads from the settings file we just updated in MainActivity
            val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val notificationLevel = sharedPrefs.getInt("NOTIFICATION_LEVEL", 80)

            // It correctly compares against the user's value
            if (batteryPct >= notificationLevel) {
                sendTargetReachedNotification(this@BatteryService)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // --- CREATE THE NOTIFICATION CHANNEL ---
        createNotificationChannel(this)

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}