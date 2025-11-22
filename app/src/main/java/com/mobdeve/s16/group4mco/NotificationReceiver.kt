package com.mobdeve.s16.group4mco

import android.R
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Get habit name and notification ID from the intent extras
        val habitName = intent.getStringExtra("HABIT_NAME") ?: "Habit"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)

        // Intent for "Mark Done" action
        val doneIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = "ACTION_DONE"
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("HABIT_NAME", habitName)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1, // unique request code
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for "Snooze" action
        val snoozeIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("HABIT_NAME", habitName)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2, // unique request code
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, "habit_channel_id")
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)  // Notification icon
            .setContentTitle("Time for your habit!")     // Title
            .setContentText("Have you completed: $habitName?") // Content text
            .setPriority(NotificationCompat.PRIORITY_HIGH)    // High priority
            .addAction(R.drawable.checkbox_on_background, "Mark Done", donePendingIntent) // Done action
            .addAction(R.drawable.ic_popup_reminder, "Snooze", snoozePendingIntent)       // Snooze action
            .setAutoCancel(true) // Auto dismiss when tapped

        // Notify the user
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
