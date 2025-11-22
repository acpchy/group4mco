package com.mobdeve.s16.group4mco

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling habits...")

            val habitDb = HabitDatabaseHelper(context)
            val allHabits = habitDb.getAllHabits()

            for (habit in allHabits) {
                try {
                    val timeParts = habit.reminderTime.split(":")
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()

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
