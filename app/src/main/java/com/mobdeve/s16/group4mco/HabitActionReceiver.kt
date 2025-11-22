package com.mobdeve.s16.group4mco

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class HabitActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val habitName = intent.getStringExtra("HABIT_NAME")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(notificationId)

        when (intent.action) {
            "ACTION_DONE" -> {
                // code for marking habit as done will be added here later
                Toast.makeText(context, "Great job completing $habitName!", Toast.LENGTH_SHORT)
                    .show()
            }

            "ACTION_SNOOZE" -> {
                // code for snoozing will be added here later
                Toast.makeText(context, "Snoozed $habitName", Toast.LENGTH_SHORT).show()
            }
        }
    }
}