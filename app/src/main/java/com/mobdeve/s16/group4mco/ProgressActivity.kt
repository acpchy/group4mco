package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group4mco.databinding.ActivityProgressBinding

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private lateinit var habitDb: HabitDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habitDb = HabitDatabaseHelper(this)

        binding.navHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        binding.navProgress.setOnClickListener {
            // already at progress activity
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
        updateProgress()
    }

    private fun updateProgress() {
        val totalHabits = habitDb.countHabits()
        val completedToday = habitDb.countCompletedToday()
        val percent = if (totalHabits == 0) 0 else (completedToday * 100 / totalHabits)

        binding.progressBar.progress = percent
        binding.progressText.text = "Today's Completion: $percent%"

        var bestStreak = 0
        val habits = habitDb.getAllHabits()
        for (h in habits) {
            val stats = habitDb.getHabitStats(h.id)
            if (stats.streak > bestStreak) bestStreak = stats.streak
        }

        binding.streakInfo.text = "🔥 Best Streak: $bestStreak days"
    }
}
