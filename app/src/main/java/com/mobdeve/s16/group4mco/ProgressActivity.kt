package com.mobdeve.s16.group4mco

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mobdeve.s16.group4mco.analytics.CategoryBreakdown
import com.mobdeve.s16.group4mco.analytics.TrendPoint
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

        binding.streakInfo.text = "🔥 Best streak: $bestStreak days"

        val weeklyStreak = habitDb.calculateRangeStreak(7)
        val monthlyStreak = habitDb.calculateRangeStreak(30)
        binding.weeklyStreakValue.text = "$weeklyStreak days"
        binding.monthlyStreakValue.text = "$monthlyStreak days"

        renderTrend(habitDb.getCompletionTrend(7), totalHabits)
        renderCategoryBreakdown(habitDb.getCategoryBreakdown())

        val points = habitDb.getTotalCompletionPoints()
        binding.pointsValue.text = "$points pts"

        val badges = buildBadges(bestStreak, points, habits.sumOf { habitDb.getHabitStats(it.id).totalCompletions })
        renderBadges(badges)
    }

    private fun renderTrend(points: List<TrendPoint>, totalHabits: Int) {
        val container = binding.trendContainer
        container.removeAllViews()

        val primaryColor = ContextCompat.getColor(this, R.color.color_primary)
        val backgroundTint = ContextCompat.getColor(this, R.color.color_surface_alt)

        val maxProgress = if (totalHabits == 0) 1 else totalHabits

        points.forEach { point ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams = params
                setPadding(0, 8, 0, 8)
            }

            val label = TextView(this).apply {
                text = point.label
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
            }

            val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = maxProgress
                progress = point.value
                progressTintList = ColorStateList.valueOf(primaryColor)
                progressBackgroundTintList = ColorStateList.valueOf(backgroundTint)
            }

            row.addView(label)
            row.addView(bar)
            container.addView(row)
        }
    }

    private fun renderCategoryBreakdown(categories: List<CategoryBreakdown>) {
        val container = binding.categoryBreakdownContainer
        container.removeAllViews()

        if (categories.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "No category data yet."
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface_secondary))
            })
            return
        }

        categories.forEach { category ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams = params
                setPadding(0, 6, 0, 6)
            }

            val label = TextView(this).apply {
                text = category.category
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val value = TextView(this).apply {
                text = "${category.completions}"
                setTextColor(ContextCompat.getColor(context, R.color.color_primary))
            }

            row.addView(label)
            row.addView(value)
            container.addView(row)
        }
    }

    private fun renderBadges(badges: List<String>) {
        val container = binding.badgesContainer
        container.removeAllViews()

        if (badges.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "Start completing habits to unlock badges!"
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface_secondary))
            })
            return
        }

        badges.forEach { badge ->
            container.addView(TextView(this).apply {
                text = "• $badge"
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
                textSize = 16f
            })
        }
    }

    private fun buildBadges(bestStreak: Int, points: Int, totalCompletions: Int): List<String> {
        val badges = mutableListOf<String>()

        if (totalCompletions >= 1) badges.add("First Step — Logged your first habit completion")
        if (bestStreak >= 7) badges.add("7-Day Streak — One full week of momentum")
        if (bestStreak >= 30) badges.add("Monthly Master — 30 days of consistency")
        if (points >= 500) badges.add("Points Champion — 500+ motivation points")
        if (totalCompletions >= 50) badges.add("Milestone Maker — 50 total completions")

        return badges
    }
}
