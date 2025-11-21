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
        val habitName = intent.getStringExtra("HABIT_NAME") ?: "Habit"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)

        val doneIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = "ACTION_DONE"
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("HABIT_NAME", habitName)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("HABIT_NAME", habitName)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "habit_channel_id")
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time for your habit!")
            .setContentText("Have you completed: $habitName?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.checkbox_on_background, "Mark Done", donePendingIntent)
            .addAction(R.drawable.ic_popup_reminder, "Snooze", snoozePendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}