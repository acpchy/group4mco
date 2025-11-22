package com.itismob.s16.group4mco

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    // Triggered when the system broadcasts an intent (like ACTION_BOOT_COMPLETED)
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the received action is indeed a system boot completion
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling habits...")

            // Initialize the database helper to access saved habits
            val habitDb = HabitDatabaseHelper(context)
            val allHabits = habitDb.getAllHabits()

            // Iterate through all habits to restore their specific alarms
            for (habit in allHabits) {
                try {
                    // Parse the stored reminder time (format expected "HH:mm")
                    val timeParts = habit.reminderTime.split(":")
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()

                    // Call the utility function to set the alarm with the AlarmManager
                    scheduleHabitNotification(
                        context,
                        habit.id,
                        habit.name,
                        hour,
                        minute
                    )
                    Log.d("BootReceiver", "Rescheduled: ${habit.name} at ${habit.reminderTime}")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error scheduling habit: ${habit.name}", e)
                }
            }
        }
    }
}
