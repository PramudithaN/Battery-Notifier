package com.pramuditha.batterynotifier // Make sure this matches your package name!

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

private const val CHANNEL_ID = "battery_channel"
private const val NOTIFICATION_ID = 1

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Battery Notifications"
        val descriptionText = "Notifications for battery charge level"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

// RENAMED and UPDATED the function
fun sendTargetReachedNotification(context: Context) {
    // --- THIS IS THE NEW LOGIC ---
    // 1. Open the settings file to find the user's preference
    val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val notificationLevel = sharedPrefs.getInt("NOTIFICATION_LEVEL", 80)

    // 2. Create a dynamic notification text using the saved value
    val notificationText = "Battery has reached $notificationLevel%. Please unplug the charger."

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
        .setContentTitle("Battery Charged!")
        // 3. Use the new dynamic text
        .setContentText(notificationText)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSilent(false)

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(NOTIFICATION_ID, builder.build())
}