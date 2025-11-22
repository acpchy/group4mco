package com.mobdeve.s16.group4mco

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group4mco.databinding.ActivityAddHabitBinding
import java.util.*

class AddHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHabitBinding
    private lateinit var db: HabitDatabaseHelper
    private var selectedTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = HabitDatabaseHelper(this)

        setupCategorySpinner()
        setupFrequencySpinner()
        setupTimePicker()

        binding.btnSaveHabit.setOnClickListener {
            saveHabit()
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Health", "Study", "Lifestyle")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupFrequencySpinner() {
        val freqs = listOf("Daily", "3x/week", "Weekdays", "Weekends")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, freqs)
        binding.spinnerFrequency.adapter = adapter
    }

    private fun setupTimePicker() {
        binding.btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, h, m ->
                selectedTime = String.format("%02d:%02d", h, m)
                binding.tvTimeSelected.text = "Reminder: $selectedTime"
            }, hour, minute, true).show()
        }
    }

    private fun saveHabit() {
        val name = binding.etHabitName.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val desc = binding.etDescription.text.toString().trim()
        val freq = binding.spinnerFrequency.selectedItem.toString()

        if (name.isEmpty()) {
            binding.etHabitName.error = "Required"
            return
        }
        if (selectedTime.isEmpty()) {
            binding.tvTimeSelected.text = "Please pick a time!"
            return
        }

        val habit = Habit(
            name = name,
            category = category,
            description = desc,
            frequency = freq,
            reminderTime = selectedTime
        )

        db.insertHabit(habit)
        finish()
    }
}
