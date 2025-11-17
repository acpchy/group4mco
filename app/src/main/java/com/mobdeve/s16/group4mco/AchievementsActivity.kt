package com.mobdeve.s16.group4mco

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mobdeve.s16.group4mco.analytics.CategoryBreakdown
import com.mobdeve.s16.group4mco.HabitStats
import com.mobdeve.s16.group4mco.databinding.ActivityAchievementsBinding
import com.mobdeve.s16.group4mco.gamification.GamificationHelper
import java.text.SimpleDateFormat
import java.util.Locale

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

        val badgeDefinitions = GamificationHelper.badgeDefinitions(bestStreak, points, totalCompletions)
        val unlockedBadges = badgeDefinitions.filter { it.unlocked }.map { "${it.title} — ${it.description}" }
        val lockedBadges = badgeDefinitions.filterNot { it.unlocked }
        binding.achievementsPoints.text = "$points pts • ${GamificationHelper.motivationMessage(bestStreak)}"

        renderUnlockedBadges(unlockedBadges)
        renderLockedBadges(lockedBadges)

        val milestones = buildMilestones(
            stats = stats,
            categories = habitDb.getCategoryBreakdown(),
            points = points
        )
        renderMilestones(milestones)
    }

    private fun renderUnlockedBadges(badges: List<String>) {
        binding.achievementsList.removeAllViews()
        if (badges.isEmpty()) {
            binding.achievementsList.addView(buildSecondaryText("Keep logging habits to unlock your first badge!"))
            return
        }

        badges.forEach { badge ->
            binding.achievementsList.addView(
                TextView(this).apply {
                    text = badge
                    textSize = 16f
                    setPadding(0, 8, 0, 8)
                    setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
                }
            )
        }
    }

    private fun renderLockedBadges(lockedBadges: List<com.mobdeve.s16.group4mco.gamification.BadgeDefinition>) {
        binding.lockedBadgesContainer.removeAllViews()
        if (lockedBadges.isEmpty()) {
            binding.lockedBadgesContainer.addView(buildSecondaryText("You’ve unlocked every badge. Time to raise the bar!"))
            return
        }

        lockedBadges.forEach { badge ->
            binding.lockedBadgesContainer.addView(
                TextView(this).apply {
                    text = "🔒 ${badge.title} — ${badge.description}"
                    textSize = 15f
                    setPadding(0, 6, 0, 6)
                    setTextColor(ContextCompat.getColor(context, R.color.color_on_surface_secondary))
                }
            )
        }
    }

    private fun buildMilestones(
        stats: List<HabitStats>,
        categories: List<CategoryBreakdown>,
        points: Int
    ): List<String> {
        val milestones = mutableListOf<String>()
        val longestStreak = stats.maxOfOrNull { it.longestStreak } ?: 0
        val totalCompletions = stats.sumOf { it.totalCompletions }
        val lastCompletion = stats.mapNotNull { it.lastCompleted }.maxOrNull()

        if (longestStreak > 0) milestones.add("🔥 Longest streak: $longestStreak days")
        if (totalCompletions > 0) milestones.add("✅ Total completions: $totalCompletions")

        if (lastCompletion != null) {
            val inputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val parsed = inputFormatter.parse(lastCompletion)
            if (parsed != null) {
                milestones.add("📅 Last logged habit: ${outputFormatter.format(parsed)}")
            }
        }

        val topCategory = categories.maxByOrNull { it.completions }
        if (topCategory != null && topCategory.completions > 0) {
            milestones.add("🏷 Top category: ${topCategory.category} (${topCategory.completions} completions)")
        }

        val weeklyTotal = habitDb.getCompletionTrend(7).sumOf { it.value }
        if (weeklyTotal > 0) milestones.add("📊 This week: $weeklyTotal completions")

        milestones.add("✨ Motivation points: $points")

        return milestones
    }

    private fun renderMilestones(milestones: List<String>) {
        binding.milestonesContainer.removeAllViews()
        if (milestones.isEmpty()) {
            binding.milestonesContainer.addView(buildSecondaryText("No milestones yet — log your first habit to begin!"))
            return
        }

        milestones.forEach { milestone ->
            binding.milestonesContainer.addView(
                TextView(this).apply {
                    text = milestone
                    textSize = 15f
                    setPadding(0, 6, 0, 6)
                    setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
                }
            )
        }
    }

    private fun buildSecondaryText(message: String): TextView {
        return TextView(this).apply {
            text = message
            setTextColor(ContextCompat.getColor(context, R.color.color_on_surface_secondary))
        }
    }
}
