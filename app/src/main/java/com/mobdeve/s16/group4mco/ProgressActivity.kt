package com.mobdeve.s16.group4mco

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ProgressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progressText = findViewById<TextView>(R.id.progressText)

        // Dummy progress
        val completion = 70
        progressBar.progress = completion
        progressText.text = "Overall Progress: $completion%"
    }
}
