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
import com.mobdeve.s16.group4mco.gamification.GamificationHelper

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private lateinit var habitDb: HabitDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habitDb = HabitDatabaseHelper(this)

        // Bottom navigation click listeners
        binding.navHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        binding.navProgress.setOnClickListener {
            // Already at ProgressActivity; do nothing
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
        // Refresh progress data when returning to this activity
        updateProgress()
    }

    /**
     * Updates all progress-related UI elements:
     * - Today's completion %
     * - Best streak
     * - Weekly & monthly streaks
     * - Completion trend chart
     * - Category breakdown
     * - Gamification points and badges
     */
    private fun updateProgress() {
        val totalHabits = habitDb.countHabits()
        val completedToday = habitDb.countCompletedToday()
        val percent = if (totalHabits == 0) 0 else (completedToday * 100 / totalHabits)

        // Update progress bar and text
        binding.progressBar.progress = percent
        binding.progressText.text = "Today's Completion: $percent%"

        // Calculate best streak across all habits
        var bestStreak = 0
        val habits = habitDb.getAllHabits()
        for (h in habits) {
            val stats = habitDb.getHabitStats(h.id)
            if (stats.streak > bestStreak) bestStreak = stats.streak
        }
        binding.streakInfo.text = "🔥 Best streak: $bestStreak days"

        // Calculate weekly and monthly streaks
        val weeklyStreak = habitDb.calculateRangeStreak(7)
        val monthlyStreak = habitDb.calculateRangeStreak(30)
        binding.weeklyStreakValue.text = "$weeklyStreak days"
        binding.monthlyStreakValue.text = "$monthlyStreak days"

        // Render trend chart and category breakdown
        renderTrend(habitDb.getCompletionTrend(7), totalHabits)
        renderCategoryBreakdown(habitDb.getCategoryBreakdown())

        // Calculate points and badges
        val points = habitDb.getTotalCompletionPoints()
        binding.pointsValue.text = "$points pts"
        val totalCompletions = habits.sumOf { habitDb.getHabitStats(it.id).totalCompletions }
        val badges = GamificationHelper.buildBadges(bestStreak, points, totalCompletions)
        renderBadges(badges)

        // Show motivational message if badges exist
        val motivation = GamificationHelper.motivationMessage(bestStreak)
        if (badges.isNotEmpty()) {
            binding.badgesContainer.addView(TextView(this).apply {
                text = motivation
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface_secondary))
                textSize = 14f
                setPadding(0, 8, 0, 0)
            })
        }
    }

    /**
     * Renders the completion trend chart (7-day) as horizontal progress bars
     */
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

    /**
     * Renders a breakdown of completions by habit category
     */
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

    /**
     * Renders badges earned by the user
     */
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
}
