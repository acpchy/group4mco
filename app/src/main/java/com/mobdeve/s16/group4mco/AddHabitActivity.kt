package com.mobdeve.s16.group4mco

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group4mco.databinding.ActivityAddHabitBinding
import java.util.*

// Activity that handles creating and saving a new habit
class AddHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHabitBinding // View binding for UI elements
    private lateinit var db: HabitDatabaseHelper            // SQLite database helper instance
    private var selectedTime = ""                          // Stores selected reminder time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database helper
        db = HabitDatabaseHelper(this)

        // Setup UI components
        setupCategorySpinner()
        setupFrequencySpinner()
        setupTimePicker()

        // Save button listener
        binding.btnSaveHabit.setOnClickListener {
            saveHabit()
        }
    }

    // Populates the category spinner with predefined categories
    private fun setupCategorySpinner() {
        val categories = listOf("Health", "Study", "Lifestyle")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter
    }

    // Populates the frequency spinner with predefined frequency options
    private fun setupFrequencySpinner() {
        val freqs = listOf("Daily", "3x/week", "Weekdays", "Weekends")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, freqs)
        binding.spinnerFrequency.adapter = adapter
    }

    // Opens a TimePickerDialog to let the user choose a reminder time
    private fun setupTimePicker() {
        binding.btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            // Show time picker dialog
            TimePickerDialog(this, { _, h, m ->
                selectedTime = String.format("%02d:%02d", h, m)
                binding.tvTimeSelected.text = "Reminder: $selectedTime"
            }, hour, minute, true).show()
        }
    }

    // Validates input fields and inserts the new habit into the database
    private fun saveHabit() {
        val name = binding.etHabitName.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val desc = binding.etDescription.text.toString().trim()
        val freq = binding.spinnerFrequency.selectedItem.toString()

        // Input validation
        if (name.isEmpty()) {
            binding.etHabitName.error = "Required"
            return
        }
        if (selectedTime.isEmpty()) {
            binding.tvTimeSelected.text = "Please pick a time!"
            return
        }

        // Create habit object
        val habit = Habit(
            name = name,
            category = category,
            description = desc,
            frequency = freq,
            reminderTime = selectedTime
        )

        // Save to database
        db.insertHabit(habit)

        // Close activity and return to previous screen
        finish()
    }
}
