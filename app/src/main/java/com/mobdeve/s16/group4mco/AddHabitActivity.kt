package com.mobdeve.s16.group4mco

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddHabitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        val habitName = findViewById<EditText>(R.id.habitName)
        val habitDesc = findViewById<EditText>(R.id.habitDesc)
        val habitFreq = findViewById<Spinner>(R.id.habitFreq)
        val saveBtn = findViewById<Button>(R.id.saveHabitBtn)

        val freqOptions = arrayOf("Daily", "Weekly", "Monthly")
        habitFreq.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, freqOptions)

        saveBtn.setOnClickListener {
            val name = habitName.text.toString().trim()
            val desc = habitDesc.text.toString().trim()
            val freq = habitFreq.selectedItem.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Habit \"$name\" added!", Toast.LENGTH_SHORT).show()
                finish() // return to Dashboard
            }
        }
    }
}
