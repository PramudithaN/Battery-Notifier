package com.pramuditha.batterynotifier // Make sure this matches your package name!

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

// A unique ID for our notification channel
private const val CHANNEL_ID = "battery_channel"
// A unique ID for the notification itself
private const val NOTIFICATION_ID = 1

fun createNotificationChannel(context: Context) {
    // Notification Channels are only needed for Android 8.0 (API 26) and higher
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Battery Notifications"
        val descriptionText = "Notifications for battery charge level"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun sendEightyPercentNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_charging) // A default battery icon
        .setContentTitle("Battery Charged!")
        .setContentText("Battery has reached 80%. Please unplug the charger.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSilent(false) // Make sure it makes a sound

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Send the notification
    notificationManager.notify(NOTIFICATION_ID, builder.build())
}