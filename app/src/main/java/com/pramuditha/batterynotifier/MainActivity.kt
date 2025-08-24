package com.pramuditha.batterynotifier

import android.content.*
import android.os.BatteryManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    // A receiver to get live battery updates while the app is open
    private val batteryInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()
            currentBatteryLevelText.text = "${batteryPct.toInt()}%"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize all UI elements
        initializeViews()

        // --- Load and Display Saved Settings ---
        val settingsPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedPercentage = settingsPrefs.getInt("NOTIFICATION_LEVEL", 80)
        updateSettingsUI(savedPercentage)

        // --- Load and Display Saved Monitoring State ---
        val statePrefs = getSharedPreferences("app_state", Context.MODE_PRIVATE)
        isServiceRunning = statePrefs.getBoolean("SERVICE_STATE", false)
        updateMonitoringUI()

        // --- UI Listeners ---
        percentageSlider.addOnChangeListener { _, value, _ ->
            sliderValueText.text = "${value.toInt()}%"
        }

        saveButton.setOnClickListener {
            val percentage = percentageSlider.value.toInt()
            settingsPrefs.edit().putInt("NOTIFICATION_LEVEL", percentage).apply()
            targetLevelText.text = "Target: $percentage%"
            Toast.makeText(this, "Notification level saved: $percentage%", Toast.LENGTH_SHORT).show()
        }

        toggleButton.setOnClickListener {
            isServiceRunning = !isServiceRunning // Toggle the state
            if (isServiceRunning) {
                startService(Intent(this, BatteryService::class.java))
            } else {
                stopService(Intent(this, BatteryService::class.java))
            }
            statePrefs.edit().putBoolean("SERVICE_STATE", isServiceRunning).apply()
            updateMonitoringUI()
        }
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
            statusText.text = "Monitoring is Active"
        } else {
            toggleButton.text = "Start Monitoring"
            statusText.text = "Monitoring is Inactive"
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryInfoReceiver)
    }
}