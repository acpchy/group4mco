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

// Activity responsible for displaying achievements, badges, and milestone stats
class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding // View binding for UI
    private lateinit var habitDb: HabitDatabaseHelper          // Database helper for habits

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database helper
        habitDb = HabitDatabaseHelper(this)

        // Gather habit statistics
        val stats = habitDb.getAllHabits().map { habitDb.getHabitStats(it.id) }
        val bestStreak = stats.maxOfOrNull { it.streak } ?: 0
        val totalCompletions = stats.sumOf { it.totalCompletions }
        val points = habitDb.getTotalCompletionPoints()

        // Retrieve badge definitions and sort into unlocked/locked
        val badgeDefinitions = GamificationHelper.badgeDefinitions(bestStreak, points, totalCompletions)
        val unlockedBadges = badgeDefinitions.filter { it.unlocked }.map { "${it.title} — ${it.description}" }
        val lockedBadges = badgeDefinitions.filterNot { it.unlocked }

        // Display points and motivational message
        binding.achievementsPoints.text = "$points pts • ${GamificationHelper.motivationMessage(bestStreak)}"

        // Render unlocked and locked badge lists
        renderUnlockedBadges(unlockedBadges)
        renderLockedBadges(lockedBadges)

        // Build milestone summary data
        val milestones = buildMilestones(
            stats = stats,
            categories = habitDb.getCategoryBreakdown(),
            points = points
        )
        renderMilestones(milestones)
    }

    // Displays the list of unlocked badges
    private fun renderUnlockedBadges(badges: List<String>) {
        binding.achievementsList.removeAllViews()

        if (badges.isEmpty()) {
            // Show empty-state message
            binding.achievementsList.addView(buildSecondaryText("Keep logging habits to unlock your first badge!"))
            return
        }

        // Add text views for each unlocked badge
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

    // Displays the locked badges with a lock icon
    private fun renderLockedBadges(lockedBadges: List<com.mobdeve.s16.group4mco.gamification.BadgeDefinition>) {
        binding.lockedBadgesContainer.removeAllViews()

        if (lockedBadges.isEmpty()) {
            // Show message if all badges have been unlocked
            binding.lockedBadgesContainer.addView(buildSecondaryText("You’ve unlocked every badge. Time to raise the bar!"))
            return
        }

        // Add locked badge entries
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

    // Builds the list of milestone strings for display
    private fun buildMilestones(
        stats: List<HabitStats>,
        categories: List<CategoryBreakdown>,
        points: Int
    ): List<String> {

        val milestones = mutableListOf<String>()

        val longestStreak = stats.maxOfOrNull { it.longestStreak } ?: 0
        val totalCompletions = stats.sumOf { it.totalCompletions }
        val lastCompletion = stats.mapNotNull { it.lastCompleted }.maxOrNull()

        // Add milestone for longest streak
        if (longestStreak > 0) milestones.add("🔥 Longest streak: $longestStreak days")

        // Add milestone for total completions
        if (totalCompletions > 0) milestones.add("✅ Total completions: $totalCompletions")

        // Add last completion date, formatted nicely
        if (lastCompletion != null) {
            val inputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val parsed = inputFormatter.parse(lastCompletion)
            if (parsed != null) {
                milestones.add("📅 Last logged habit: ${outputFormatter.format(parsed)}")
            }
        }

        // Add most completed habit category
        val topCategory = categories.maxByOrNull { it.completions }
        if (topCategory != null && topCategory.completions > 0) {
            milestones.add("🏷 Top category: ${topCategory.category} (${topCategory.completions} completions)")
        }

        // Add weekly habit total
        val weeklyTotal = habitDb.getCompletionTrend(7).sumOf { it.value }
        if (weeklyTotal > 0) milestones.add("📊 This week: $weeklyTotal completions")

        // Always add motivation points milestone
        milestones.add("✨ Motivation points: $points")

        return milestones
    }

    // Displays the milestone list
    private fun renderMilestones(milestones: List<String>) {
        binding.milestonesContainer.removeAllViews()

        if (milestones.isEmpty()) {
            // Show empty-state message
            binding.milestonesContainer.addView(buildSecondaryText("No milestones yet — log your first habit to begin!"))
            return
        }

        // Add each milestone as a text entry
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

    // Helper for creating a secondary-style text view (gray text)
    private fun buildSecondaryText(message: String): TextView {
        return TextView(this).apply {
            text = message
            setTextColor(ContextCompat.getColor(context, R.color.color_on_surface_secondary))
        }
    }
}
