package com.pramuditha.batterynotifier // Make sure this matches your package name!

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Safety check
        if (context == null || intent == null) return

        // Check which event was received
        if (intent.action == Intent.ACTION_POWER_CONNECTED) {
            Log.d("PowerReceiver", "Charger connected! Starting service.")
            val serviceIntent = Intent(context, BatteryService::class.java)
            context.startService(serviceIntent)
        } else if (intent.action == Intent.ACTION_POWER_DISCONNECTED) {
            Log.d("PowerReceiver", "Charger disconnected! Stopping service.")
            val serviceIntent = Intent(context, BatteryService::class.java)
            context.stopService(serviceIntent)
        }
    }
}