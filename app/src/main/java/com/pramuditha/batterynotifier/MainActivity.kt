package com.pramuditha.batterynotifier

import android.content.Context // NEW: Import for Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest

class MainActivity : AppCompatActivity() {

    private var isServiceRunning = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) { /* Permission granted */ } else { /* Permission denied */ }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askNotificationPermission()

        val statusText = findViewById<TextView>(R.id.statusText)
        val toggleButton = findViewById<Button>(R.id.toggleButton)

        // --- NEW: Read the saved state when the app starts ---
        val sharedPrefs = getSharedPreferences("app_state", Context.MODE_PRIVATE)
        isServiceRunning = sharedPrefs.getBoolean("SERVICE_STATE", false)
        updateUI(statusText, toggleButton) // Update UI based on saved state

        toggleButton.setOnClickListener {
            if (isServiceRunning) {
                // --- STOP ---
                isServiceRunning = false
                val serviceIntent = Intent(this, BatteryService::class.java)
                stopService(serviceIntent)
            } else {
                // --- START ---
                isServiceRunning = true
                val serviceIntent = Intent(this, BatteryService::class.java)
                startService(serviceIntent)
            }
            // --- NEW: Save the new state every time the button is clicked ---
            sharedPrefs.edit().putBoolean("SERVICE_STATE", isServiceRunning).apply()
            updateUI(statusText, toggleButton) // Update UI to reflect the change
        }
    }

    // --- NEW: Created a helper function to avoid repeating UI code ---
    private fun updateUI(statusText: TextView, toggleButton: Button) {
        if (isServiceRunning) {
            toggleButton.text = "Stop Monitoring"
            statusText.text = "Monitoring is Active"
        } else {
            toggleButton.text = "Start Monitoring"
            statusText.text = "Monitoring is Inactive"
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}