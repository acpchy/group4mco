package com.itismob.s16.group4mco

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itismob.s16.group4mco.databinding.ActivityEditHabitBinding
import java.util.*

// Activity for editing an existing habit
class EditHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditHabitBinding
    private lateinit var habitDb: HabitDatabaseHelper
    private var habitId: Int = -1
    private var selectedReminderTime: String? = null

    // Predefined categories and frequencies for habits
    private val categories = listOf("Health", "Study", "Lifestyle")
    private val frequencies = listOf("Daily", "3x/week", "Weekdays", "Weekends")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habitDb = HabitDatabaseHelper(this)

        // Set up the category spinner with predefined categories
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Set up the frequency spinner with predefined frequencies
        val frequencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencies)
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrequency.adapter = frequencyAdapter

        // Get habit ID from intent; if invalid, close activity
        habitId = intent.getIntExtra("habitId", -1)
        if (habitId == -1) {
            Toast.makeText(this, "Invalid habit ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load habit data from database and populate UI
        loadHabitData(habitId)

        // Set up time picker button
        binding.btnPickTime.setOnClickListener {
            showTimePicker()
        }

        // Save updated habit
        binding.btnSaveHabit.setOnClickListener {
            saveHabit()
        }

        // Delete habit
        binding.btnDeleteHabit.setOnClickListener {
            deleteHabit()
        }
    }

    // Load habit data from database into UI components
    private fun loadHabitData(id: Int) {
        val habit = habitDb.getAllHabits().find { it.id == id }
        if (habit == null) {
            Toast.makeText(this, "Habit not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.etHabitName.setText(habit.name)
        binding.etDescription.setText(habit.description ?: "")

        // Set the correct selection in category spinner
        val catIndex = categories.indexOf(habit.category)
        if (catIndex >= 0) binding.spinnerCategory.setSelection(catIndex)

        // Set the correct selection in frequency spinner
        val freqIndex = frequencies.indexOf(habit.frequency)
        if (freqIndex >= 0) binding.spinnerFrequency.setSelection(freqIndex)

        // Display the reminder time
        selectedReminderTime = habit.reminderTime
        binding.tvTimeSelected.text = selectedReminderTime ?: "No time selected"
    }

    // Show a TimePicker dialog to select a reminder time
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            selectedReminderTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.tvTimeSelected.text = selectedReminderTime
        }, hour, minute, true)

        timePicker.show()
    }

    // Save the updated habit to the database
    private fun saveHabit() {
        val name = binding.etHabitName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem as String
        val frequency = binding.spinnerFrequency.selectedItem as String
        val newTime = binding.tvTimeSelected.text.toString()
        val timeParts = newTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
            return
        }

        // Create an updated habit object
        val updatedHabit = Habit(
            id = habitId,
            name = name,
            category = category,
            description = description,
            frequency = frequency,
            reminderTime = selectedReminderTime ?: ""
        )

        // Update habit in database and show result
        val rowsAffected = habitDb.updateHabit(updatedHabit)
        scheduleHabitNotification(
            context = this,
            habitId = habitId,
            habitName = name,
            hour = hour,
            minute = minute
        )
        if (rowsAffected > 0) {
            Toast.makeText(this, "Habit updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to update habit", Toast.LENGTH_SHORT).show()
        }
    }

    // Delete the habit from the database
    private fun deleteHabit() {
        val deleted = habitDb.deleteHabit(habitId)
        if (deleted > 0) {
            Toast.makeText(this, "Habit deleted", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to delete habit", Toast.LENGTH_SHORT).show()
        }
    }
}
