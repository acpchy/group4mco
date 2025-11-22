package com.mobdeve.s16.group4mco

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.text.intl.Locale
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.text.format

class HabitActionReceiver : BroadcastReceiver() {
    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val habitName = intent.getStringExtra("HABIT_NAME")
        val habitId = intent.getIntExtra("HABIT_ID", -1)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(notificationId)

        when (intent.action) {
            "ACTION_DONE" -> {
                if (habitId != -1) {
                    val habitDb = HabitDatabaseHelper(context)
                    val today = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(Date())

                    // Update database
                    habitDb.markCompleted(habitId, today)

                    Toast.makeText(context, "Great job completing $habitName!", Toast.LENGTH_SHORT).show()

                    // Optional: Send a broadcast to DashboardActivity to refresh the UI immediately
                    val refreshIntent = Intent("com.mobdeve.s16.group4mco.REFRESH_DASHBOARD")
                    context.sendBroadcast(refreshIntent)
                }
                Toast.makeText(context, "Great job completing $habitName!", Toast.LENGTH_SHORT)
                    .show()
            }

            "ACTION_SNOOZE" -> {
                val snoozeTime = System.currentTimeMillis() + (2 *60 * 1000)

                // Create an intent to re-trigger the NotificationReceiver
                val snoozeIntent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("HABIT_ID", habitId)
                    putExtra("HABIT_NAME", habitName)
                    putExtra("NOTIFICATION_ID", notificationId)
                    action = "SNOOZE_EVENT"
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                // Android 12+ Permission Safety Check
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            snoozeTime,
                            pendingIntent
                        )
                    } else {
                        // Fallback for no permission: Inexact alarm
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            snoozeTime,
                            pendingIntent
                        )
                    }
                } else {
                    // Android 6 - 11
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime,
                        pendingIntent
                    )
                }

                Toast.makeText(context, "Snoozed $habitName for 2 minutes", Toast.LENGTH_SHORT).show()
            }
        }
    }
}