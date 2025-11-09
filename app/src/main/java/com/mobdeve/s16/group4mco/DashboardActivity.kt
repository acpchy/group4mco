package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val addHabitBtn = findViewById<Button>(R.id.addHabitBtn)
        val progressBtn = findViewById<Button>(R.id.progressBtn)
        val achievementsBtn = findViewById<Button>(R.id.achievementsBtn)
        val settingsBtn = findViewById<Button>(R.id.settingsBtn)

        addHabitBtn.setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }

        progressBtn.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }

        achievementsBtn.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
