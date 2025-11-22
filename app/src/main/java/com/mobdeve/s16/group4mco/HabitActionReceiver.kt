package com.mobdeve.s16.group4mco

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

// BroadcastReceiver to handle habit notification actions such as marking as done or snoozing
class HabitActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        // Retrieve the notification ID and habit name from the Intent extras
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val habitName = intent.getStringExtra("HABIT_NAME")

        // Get the system NotificationManager to cancel the notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cancel the notification when the action is received
        notificationManager.cancel(notificationId)

        // Handle actions based on the Intent's action string
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
