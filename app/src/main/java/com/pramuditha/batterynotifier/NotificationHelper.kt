package com.pramuditha.batterynotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

private const val CHANNEL_ID = "battery_channel"
private const val NOTIFICATION_ID = 1

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Battery Notifications"
        val descriptionText = "Notifications for battery charge level"
        // IMPORTANT: Set importance to HIGH for full-screen intents
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
//
fun sendTargetReachedNotification(context: Context) {
    // --- NEW: Create an Intent to launch MainActivity ---
    val fullScreenIntent = Intent(context, MainActivity::class.java)
    val fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
        fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val notificationLevel = sharedPrefs.getInt("NOTIFICATION_LEVEL", 80)
    val notificationText = "Battery has reached your set level of $notificationLevel%. Please unplug the charger."

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
        .setContentTitle("Battery Charged!")
        .setContentText(notificationText)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        // --- NEW: Attach the full-screen intent ---
        .setFullScreenIntent(fullScreenPendingIntent, true)
        .setSilent(false)

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(NOTIFICATION_ID, builder.build())
}