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

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()

            Log.d("BatteryService", "Current Battery Level: $batteryPct%")

            if (batteryPct >= 80) {
                Log.d("BatteryService", "BATTERY IS AT 80% OR HIGHER! SENDING NOTIFICATION.")
                // --- SEND THE NOTIFICATION ---
                sendEightyPercentNotification(this@BatteryService)
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