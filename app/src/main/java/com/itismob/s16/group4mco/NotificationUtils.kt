package com.itismob.s16.group4mco

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

@SuppressLint("ScheduleExactAlarm")
fun scheduleHabitNotification(context: Context, habitId: Int, habitName: String, hour: Int, minute: Int) {
    // Create Notification Channel (Required for Android 8.0+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Habit Reminders"
        val descriptionText = "Channel for habit notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("habit_channel_id", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Set the time
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)

        // If time has passed today, schedule for tomorrow
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    // Create Intent for Alarm
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("HABIT_NAME", habitName)
        putExtra("HABIT_ID", habitId) // Critical: Pass the ID so ActionReceiver can find it later
        putExtra("NOTIFICATION_ID", habitId) // Use habitId as unique notification ID
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        habitId, // Unique request code
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Schedule Alarm
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
