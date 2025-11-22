package com.itismob.s16.group4mco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.android.material.card.MaterialCardView
import com.itismob.s16.group4mco.databinding.ActivityDashboardBinding
import java.text.SimpleDateFormat
import java.util.*

// Main dashboard activity showing user's habits, greeting, and navigation
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding // View binding
    private lateinit var habitDb: HabitDatabaseHelper       // Database helper for habits
    private var habits = listOf<Habit>()                   // List of habits loaded from DB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habitDb = HabitDatabaseHelper(this)
        val userPrefs = getSharedPreferences("UserSettings", MODE_PRIVATE)
        rescheduleAllAlarms()

        // Set greeting and motivation message
        binding.tvGreeting.text = "Hi, ${userPrefs.getString("LOGGED_IN_USER_FIRSTNAME", null)}!"
        binding.tvMotivation.text = "Let's make habits together!"

        // Android 13 now requires the user to grant the app permission to send notifications
        // Handle notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            } else {
                userPrefs.edit { putBoolean("SEND_NOTIFICATIONS", true) }
            }
        } else {
            userPrefs.edit { putBoolean("SEND_NOTIFICATIONS", true) }
        }

        // Display today's date
        binding.tvDate.text = todayDate()

        // Button click listeners for adding habits or checking progress
        binding.btnAddHabit.setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }
        binding.btnCheckProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }

        // Navigation bar listeners
        binding.navHome.setOnClickListener {
            // already home
        }
        binding.navProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
        binding.navAchievements.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }
        binding.navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadHabits() // Reload habits whenever the activity resumes
    }
    // Set the "SEND_NOTIFICATIONS" SharedPreferences to true when the user presses "Allow"
    // when their Android device (at least running Android 13) informs that the app needs to
    // permission to send notifications
    // Handles the result from notification permission dialog
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            val userPrefs = getSharedPreferences("UserSettings", MODE_PRIVATE)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                userPrefs.edit { putBoolean("SEND_NOTIFICATIONS", true) }
            }
        }
    }

    // Loads all habits from the database and displays them in cards
    private fun loadHabits() {
        binding.habitContainer.removeAllViews()
        habits = habitDb.getAllHabits()
        val today = todayDateDatabaseFormat()

        // UI styling values
        val margin = resources.getDimensionPixelSize(R.dimen.spacing_3)
        val padding = resources.getDimensionPixelSize(R.dimen.card_padding)
        val cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
        val elevation = resources.getDimension(R.dimen.card_elevation)
        val surfaceColor = ContextCompat.getColor(this, R.color.color_surface)
        val textSecondary = ContextCompat.getColor(this, R.color.color_on_surface_secondary)

        // Loop through each habit and create its card
        for ((index, habit) in habits.withIndex()) {
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, if (index == 0) margin else margin / 2, 0, margin)
                }
                radius = cornerRadius
                cardElevation = elevation
                setCardBackgroundColor(surfaceColor)
                setContentPadding(padding, padding, padding, padding)
            }

            val habitLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // CheckBox for marking habit completion
            val cb = CheckBox(this).apply {
                text = habit.name
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                setOnCheckedChangeListener(null)
                isChecked = habitDb.isCompletedForDate(habit.id, today)

                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        habitDb.markCompleted(habit.id, today)
                    } else {
                        habitDb.removeCompletion(habit.id, today)
                    }
                }

                // Long press opens edit habit activity
                setOnLongClickListener {
                    val intent = Intent(this@DashboardActivity, EditHabitActivity::class.java)
                    intent.putExtra("habitId", habit.id)
                    startActivity(intent)
                    true
                }
            }

            // TextView for habit description
            val desc = TextView(this).apply {
                text = habit.description ?: ""
                textSize = 14f
                setTextColor(textSecondary)
                setPadding(8, 0, 8, 8)
            }

            // Horizontal row for action buttons
            val actionsRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 0)
            }

            // Edit habit button
            val editButton = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_edit)
                background = null
                setColorFilter(ContextCompat.getColor(context, R.color.color_primary))
                contentDescription = "Edit habit"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 16, 0) }
                setOnClickListener { launchEditHabit(habit.id) }
            }

            // Delete habit button
            val deleteButton = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                background = null
                setColorFilter(ContextCompat.getColor(context, R.color.color_error))
                contentDescription = "Delete habit"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener { confirmDeleteHabit(habit) }
            }

            actionsRow.addView(editButton)
            actionsRow.addView(deleteButton)

            // Add views to habit layout
            habitLayout.addView(cb)
            habitLayout.addView(desc)
            habitLayout.addView(actionsRow)

            // Add habit layout to card, then card to container
            card.addView(habitLayout)
            binding.habitContainer.addView(card)
        }
    }

    // Returns today's date in display format (e.g., "November 22, 2025")
    private fun todayDate(): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    // Returns today's date in database format (e.g., "2025-11-22")
    private fun todayDateDatabaseFormat(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Launches the EditHabitActivity for a given habit
    private fun launchEditHabit(habitId: Int) {
        val intent = Intent(this, EditHabitActivity::class.java)
        intent.putExtra("habitId", habitId)
        startActivity(intent)
    }

    // Prompts user with confirmation dialog before deleting a habit
    private fun confirmDeleteHabit(habit: Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete \"${habit.name}\"?")
            .setMessage("This will remove the habit and its history.")
            .setPositiveButton("Delete") { _, _ ->
                habitDb.deleteHabit(habit.id)
                loadHabits()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun rescheduleAllAlarms() {
        val habitDb = HabitDatabaseHelper(this)
        val allHabits = habitDb.getAllHabits()

        for (habit in allHabits) {
            try {
                val timeParts = habit.reminderTime.split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()

                scheduleHabitNotification(
                    context = this,
                    habitId = habit.id,
                    habitName = habit.name,
                    hour = hour,
                    minute = minute
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
