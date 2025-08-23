package com.pramuditha.batterynotifier

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isServiceRunning = false

    // UI elements for the new feature
    private lateinit var percentageInput: EditText
    private lateinit var saveButton: Button

    // UI elements for the old feature
    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize all UI elements
        percentageInput = findViewById(R.id.percentageInput)
        saveButton = findViewById(R.id.saveButton)
        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)

        // --- LOGIC FOR CUSTOM PERCENTAGE ---
        val settingsPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedPercentage = settingsPrefs.getInt("NOTIFICATION_LEVEL", 80)
        percentageInput.setText(savedPercentage.toString())

        saveButton.setOnClickListener {
            val inputText = percentageInput.text.toString()
            if (inputText.isNotEmpty()) {
                val percentage = inputText.toInt()
                settingsPrefs.edit().putInt("NOTIFICATION_LEVEL", percentage).apply()
                Toast.makeText(this, "Notification level saved: $percentage%", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show()
            }
        }

        // --- LOGIC FOR START/STOP MONITORING AND STATE SAVING ---
        val statePrefs = getSharedPreferences("app_state", Context.MODE_PRIVATE)
        isServiceRunning = statePrefs.getBoolean("SERVICE_STATE", false)
        updateMonitoringUI() // Update UI based on saved state

        toggleButton.setOnClickListener {
            isServiceRunning = !isServiceRunning // Toggle the state
            if (isServiceRunning) {
                // If we are now running, start the service
                startService(Intent(this, BatteryService::class.java))
            } else {
                // If we are now stopped, stop the service
                stopService(Intent(this, BatteryService::class.java))
            }
            // Save the new state and update the UI
            statePrefs.edit().putBoolean("SERVICE_STATE", isServiceRunning).apply()
            updateMonitoringUI()
        }
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
}