package com.pramuditha.batterynotifier

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    // --- UI Elements ---
    private lateinit var currentBatteryLevelText: TextView
    private lateinit var targetLevelText: TextView
    private lateinit var sliderValueText: TextView
    private lateinit var percentageSlider: Slider
    private lateinit var saveButton: Button
    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button

    // --- State Variables ---
    private var isServiceRunning = false

    // Runtime notification permission launcher for Android 13+ (API 33+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startMonitoringService()
        } else {
            Toast.makeText(
                this,
                "Notification permission is required to alert you when your target battery level is reached.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Receiver to update battery level in real-time while the screen is open
    private val batteryInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                val batteryPct = (level * 100 / scale.toFloat()).toInt()
                currentBatteryLevelText.text = "$batteryPct%"
            }
        }
    }

    // Receiver to keep UI in sync when service state changes (e.g. stopped from notification)
    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            syncServiceState()
            updateMonitoringUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        initializeViews()

        // Create notification channels early
        NotificationHelper.createNotificationChannels(this)

        // Load saved target percentage
        val settingsPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedPercentage = settingsPrefs.getInt("NOTIFICATION_LEVEL", 80)
        updateSettingsUI(savedPercentage)

        // Load saved monitoring state
        syncServiceState()
        updateMonitoringUI()

        // Slider change listener with haptic feedback
        percentageSlider.addOnChangeListener { _, value, fromUser ->
            sliderValueText.text = "${value.toInt()}%"
            if (fromUser) {
                percentageSlider.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }

        // Save target level button
        saveButton.setOnClickListener {
            val percentage = percentageSlider.value.toInt()
            settingsPrefs.edit().putInt("NOTIFICATION_LEVEL", percentage).apply()
            targetLevelText.text = "Target: $percentage%"

            // If the service is running, notify it to reload the new threshold
            if (isServiceRunning) {
                val refreshIntent = Intent(this, BatteryService::class.java)
                ContextCompat.startForegroundService(this, refreshIntent)
            }

            Toast.makeText(this, "Target level saved: $percentage%", Toast.LENGTH_SHORT).show()
        }

        // Toggle button: Start or Stop Monitoring
        toggleButton.setOnClickListener {
            if (isServiceRunning) {
                stopMonitoringService()
            } else {
                requestAndStartMonitoring()
            }
        }
    }

    private fun requestAndStartMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        startMonitoringService()
    }

    private fun startMonitoringService() {
        val serviceIntent = Intent(this, BatteryService::class.java).apply {
            action = BatteryService.ACTION_START_MONITORING
        }
        ContextCompat.startForegroundService(this, serviceIntent)
        isServiceRunning = true
        saveServiceState(true)
        updateMonitoringUI()
        Toast.makeText(this, "Monitoring started", Toast.LENGTH_SHORT).show()
    }

    private fun stopMonitoringService() {
        val serviceIntent = Intent(this, BatteryService::class.java).apply {
            action = BatteryService.ACTION_STOP_MONITORING
        }
        startService(serviceIntent)
        isServiceRunning = false
        saveServiceState(false)
        updateMonitoringUI()
        Toast.makeText(this, "Monitoring stopped", Toast.LENGTH_SHORT).show()
    }

    private fun syncServiceState() {
        val statePrefs = getSharedPreferences("app_state", Context.MODE_PRIVATE)
        isServiceRunning = BatteryService.isServiceRunning || statePrefs.getBoolean("SERVICE_STATE", false)
    }

    private fun saveServiceState(active: Boolean) {
        val statePrefs = getSharedPreferences("app_state", Context.MODE_PRIVATE)
        statePrefs.edit().putBoolean("SERVICE_STATE", active).apply()
    }

    private fun initializeViews() {
        currentBatteryLevelText = findViewById(R.id.currentBatteryLevelText)
        targetLevelText = findViewById(R.id.targetLevelText)
        sliderValueText = findViewById(R.id.sliderValueText)
        percentageSlider = findViewById(R.id.percentageSlider)
        saveButton = findViewById(R.id.saveButton)
        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)
    }

    private fun updateSettingsUI(percentage: Int) {
        targetLevelText.text = "Target: $percentage%"
        sliderValueText.text = "$percentage%"
        percentageSlider.value = percentage.toFloat()
    }

    private fun updateMonitoringUI() {
        if (isServiceRunning) {
            toggleButton.text = "Stop Monitoring"
            toggleButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red_stop)
            toggleButton.setTextColor(Color.WHITE)
            statusText.text = "Monitoring is Active"
        } else {
            toggleButton.text = "Start Monitoring"
            toggleButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.purple_start)
            toggleButton.setTextColor(Color.WHITE)
            statusText.text = "Monitoring is Inactive"
        }
    }

    override fun onResume() {
        super.onResume()
        syncServiceState()
        updateMonitoringUI()

        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val stateFilter = IntentFilter(BatteryService.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serviceStateReceiver, stateFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(serviceStateReceiver, stateFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(batteryInfoReceiver)
        } catch (_: Exception) {}

        try {
            unregisterReceiver(serviceStateReceiver)
        } catch (_: Exception) {}
    }
}
