package com.mobdeve.s16.group4mco

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group4mco.databinding.ActivityEditHabitBinding
import java.util.*

class EditHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditHabitBinding
    private lateinit var habitDb: HabitDatabaseHelper
    private var habitId: Int = -1
    private var selectedReminderTime: String? = null

    private val categories = listOf("Health", "Study", "Lifestyle")
    private val frequencies = listOf("Daily", "3x/week", "Weekdays", "Weekends")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habitDb = HabitDatabaseHelper(this)

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        val frequencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencies)
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrequency.adapter = frequencyAdapter

        habitId = intent.getIntExtra("habitId", -1)
        if (habitId == -1) {
            Toast.makeText(this, "Invalid habit ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadHabitData(habitId)

        binding.btnPickTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSaveHabit.setOnClickListener {
            saveHabit()
        }

        binding.btnDeleteHabit.setOnClickListener {
            deleteHabit()
        }
    }

    private fun loadHabitData(id: Int) {
        val habit = habitDb.getAllHabits().find { it.id == id }
        if (habit == null) {
            Toast.makeText(this, "Habit not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.etHabitName.setText(habit.name)
        binding.etDescription.setText(habit.description ?: "")

        val catIndex = categories.indexOf(habit.category)
        if (catIndex >= 0) binding.spinnerCategory.setSelection(catIndex)

        val freqIndex = frequencies.indexOf(habit.frequency)
        if (freqIndex >= 0) binding.spinnerFrequency.setSelection(freqIndex)

        selectedReminderTime = habit.reminderTime
        binding.tvTimeSelected.text = selectedReminderTime ?: "No time selected"
    }

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

    private fun saveHabit() {
        val name = binding.etHabitName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem as String
        val frequency = binding.spinnerFrequency.selectedItem as String

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedHabit = Habit(
            id = habitId,
            name = name,
            category = category,
            description = description,
            frequency = frequency,
            reminderTime = selectedReminderTime ?: ""
        )

        val rowsAffected = habitDb.updateHabit(updatedHabit)
        if (rowsAffected > 0) {
            Toast.makeText(this, "Habit updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to update habit", Toast.LENGTH_SHORT).show()
        }
    }

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
