package com.mobdeve.s16.group4mco

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitName = intent.getStringExtra("HABIT_NAME") ?: "Habit"
        val habitId = intent.getIntExtra("HABIT_ID", -1)
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        var habitTime = ""
        val userPrefs = context.getSharedPreferences("UserSettings", MODE_PRIVATE)

        // Check if the user has disabled notifications in settings.
        // If disabled, we still need to schedule the next alarm to keep the cycle alive, but
        // we return early to avoid showing the notification.
        if (!userPrefs.getBoolean("SEND_NOTIFICATIONS", true)) {
            if (habitId != -1) {
                val habitDb = HabitDatabaseHelper(context)
                val habit = habitDb.getHabitById(habitId)
                if (habit != null) {
                    val timeParts = habit.reminderTime.split(":")
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()
                    scheduleHabitNotification(context, habitId, habitName, hour, minute)
                }
            }
            return
        }

        if (habitId != -1) {
            val habitDb = HabitDatabaseHelper(context)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val habit = habitDb.getHabitById(habitId)

            // Schedule the NEXT alarm for tomorrow
            // This ensures the cycle repeats every day, effectively creating a repeating alarm logic.
            if (habit != null) {
                habitTime = habit.reminderTime
                val timeParts = habit.reminderTime.split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                scheduleHabitNotification(context, habitId, habitName, hour, minute)
            }

            // Check if the action is a snooze event
            // If the action is NOT a snooze event, we check if the habit is already
            // completed for today. If it is, we suppress the notification.
            if (intent.action != "SNOOZE_EVENT") {
                if (habitDb.isCompletedForDate(habitId, today)) {
                    return
                }

                // Check Frequency
                // Verify if the current day matches the user's selected frequency (e.g., Weekdays, 3x/week).
                if (habit != null) {
                    val calendar = Calendar.getInstance()
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    // Sun=1, Mon=2, Tue=3, Wed=4, Thu=5, Fri=6, Sat=7

                    val shouldNotify = when (habit.frequency) {
                        "Daily" -> true
                        "Weekdays" -> dayOfWeek in 2..6 // Mon to Fri
                        "Weekends" -> dayOfWeek == 1 || dayOfWeek == 7 // Sun or Sat
                        "3x/week" -> dayOfWeek == 2 || dayOfWeek == 4 || dayOfWeek == 6 // Mon, Wed, Fri
                        else -> true
                    }

                    if (!shouldNotify) {
                        return
                    }
                }
            }

        }

        // Build and Show Notification
        // Prepare intents for "Mark Done" and "Snooze" actions directly from the notification bar.
        val doneIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = "ACTION_DONE"
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("HABIT_NAME", habitName)
            putExtra("HABIT_ID", habitId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("HABIT_NAME", habitName)
            putExtra("HABIT_ID", habitId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "habit_channel_id")
            .setSmallIcon(R.drawable.consistify_icon_notification)
            .setContentTitle("$habitName at $habitTime")
            .setContentText("Are you done with this habit or you want to snooze this habit for an additional ${userPrefs.getInt("SNOOZE_TIME", 5)} minute(s)?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(android.R.drawable.checkbox_on_background, "Mark Done", donePendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze", snoozePendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
