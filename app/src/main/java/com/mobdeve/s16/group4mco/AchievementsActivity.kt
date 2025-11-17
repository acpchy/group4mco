package com.mobdeve.s16.group4mco

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mobdeve.s16.group4mco.databinding.ActivityAchievementsBinding
import com.mobdeve.s16.group4mco.gamification.GamificationHelper

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding
    private lateinit var habitDb: HabitDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habitDb = HabitDatabaseHelper(this)

        val stats = habitDb.getAllHabits().map { habitDb.getHabitStats(it.id) }
        val bestStreak = stats.maxOfOrNull { it.streak } ?: 0
        val totalCompletions = stats.sumOf { it.totalCompletions }
        val points = habitDb.getTotalCompletionPoints()

        val badges = GamificationHelper.buildBadges(bestStreak, points, totalCompletions)

        binding.achievementsPoints.text = "$points pts • ${GamificationHelper.motivationMessage(bestStreak)}"

        binding.achievementsList.removeAllViews()
        if (badges.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "Keep logging habits to unlock your first badge!"
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface_secondary))
            }
            binding.achievementsList.addView(emptyView)
        } else {
            badges.forEach { badge ->
                val badgeView = TextView(this).apply {
                    text = badge
                    textSize = 16f
                    setPadding(0, 8, 0, 8)
                    setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
                }
                binding.achievementsList.addView(badgeView)
            }
        }
    }
}
