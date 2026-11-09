package com.pramuditha.batterynotifier

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log

class BatteryService : Service() {

    companion object {
        const val TAG = "BatteryService"
        const val ACTION_START_MONITORING = "com.pramuditha.batterynotifier.ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.pramuditha.batterynotifier.ACTION_STOP_MONITORING"
        const val ACTION_STATE_CHANGED = "com.pramuditha.batterynotifier.ACTION_STATE_CHANGED"

        @Volatile
        var isServiceRunning = false
            private set
    }

    private var hasAlerted = false
    private var isReceiverRegistered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                processBatteryUpdate(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BatteryService onCreate called")

        NotificationHelper.createNotificationChannels(this)

        // Read initial battery status to immediately display valid information
        val initialBatteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val (batteryPct, isCharging) = extractBatteryInfo(initialBatteryIntent)

        val settingsPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val targetLevel = settingsPrefs.getInt("NOTIFICATION_LEVEL", 80)

        val initialNotification = NotificationHelper.buildForegroundNotification(
            this,
            batteryPct,
            targetLevel,
            isCharging
        )

        // Start Foreground immediately within 5 seconds of service launch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            startForeground(
                NotificationHelper.NOTIFICATION_ID_FOREGROUND,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID_FOREGROUND, initialNotification)
        }

        // Register dynamic receiver for live battery changes
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
        isReceiverRegistered = true

        isServiceRunning = true
        updateServiceStatePreference(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "BatteryService onStartCommand with action: ${intent?.action}")

        if (intent?.action == ACTION_STOP_MONITORING) {
            stopMonitoring()
            return START_NOT_STICKY
        }

        isServiceRunning = true
        updateServiceStatePreference(true)

        // Refresh with latest settings and battery status
        val currentBatteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        processBatteryUpdate(currentBatteryIntent)

        return START_STICKY
    }

    private fun processBatteryUpdate(intent: Intent?) {
        val (batteryPct, isCharging) = extractBatteryInfo(intent)

        val settingsPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val targetLevel = settingsPrefs.getInt("NOTIFICATION_LEVEL", 80)

        // Update the ongoing foreground notification with current percentage and status
        val updatedNotification = NotificationHelper.buildForegroundNotification(
            this,
            batteryPct,
            targetLevel,
            isCharging
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationHelper.NOTIFICATION_ID_FOREGROUND, updatedNotification)

        // Check if target is reached while charging
        if (batteryPct >= targetLevel && isCharging) {
            if (!hasAlerted) {
                hasAlerted = true
                Log.d(TAG, "Target reached ($batteryPct%). Sending high-priority alert notification.")
                NotificationHelper.sendTargetReachedNotification(this, batteryPct, targetLevel)
            }
        } else if (batteryPct < targetLevel) {
            // Reset alert latch so it can trigger again if charge dips and climbs back
            hasAlerted = false
        }
    }

    private fun extractBatteryInfo(intent: Intent?): Pair<Int, Boolean> {
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        return Pair(batteryPct, isCharging)
    }

    private fun stopMonitoring() {
        Log.d(TAG, "Stopping BatteryService monitoring")
        isServiceRunning = false
        updateServiceStatePreference(false)

        NotificationHelper.cancelAlertNotification(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        stopSelf()
    }

    private fun updateServiceStatePreference(active: Boolean) {
        val statePrefs = getSharedPreferences("app_state", Context.MODE_PRIVATE)
        statePrefs.edit().putBoolean("SERVICE_STATE", active).apply()
        sendBroadcast(Intent(ACTION_STATE_CHANGED))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "BatteryService onDestroy called")

        if (isReceiverRegistered) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering batteryReceiver", e)
            }
            isReceiverRegistered = false
        }

        isServiceRunning = false
        updateServiceStatePreference(false)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
