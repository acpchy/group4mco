package com.itismob.s16.group4mco.gamification

// Data class representing a badge's information and whether it's unlocked
data class BadgeDefinition(
    val title: String,          // Name of the badge
    val description: String,    // What the badge is for
    val unlocked: Boolean       // Whether the user has unlocked it
)

// Helper object for managing badges and gamification-related logic
object GamificationHelper {

    // Generates the list of all badge definitions with their unlocked status
    fun badgeDefinitions(bestStreak: Int, points: Int, totalCompletions: Int): List<BadgeDefinition> {
        val definitions = listOf(
            BadgeDefinition(
                "First Step",
                "Log your first habit completion",
                totalCompletions >= 1                // Unlocks after 1 completion
            ),
            BadgeDefinition(
                "Warm-Up",
                "Hold a 3-day streak",
                bestStreak >= 3                      // Unlocks at 3-day streak
            ),
            BadgeDefinition(
                "7-Day Streak",
                "Complete habits for 7 days straight",
                bestStreak >= 7                      // Unlocks at 7-day streak
            ),
            BadgeDefinition(
                "Monthly Master",
                "Stay consistent for 30 consecutive days",
                bestStreak >= 30                     // Unlocks at 30-day streak
            ),
            BadgeDefinition(
                "Points Champion",
                "Earn 500 motivation points",
                points >= 500                        // Unlocks at 500 accumulated points
            ),
            BadgeDefinition(
                "Milestone Maker",
                "Reach 50 total completions",
                totalCompletions >= 50               // Unlocks after 50 completions
            )
        )
        return definitions
    }

    // Returns a list of badge titles + descriptions for badges the user has unlocked
    fun buildBadges(bestStreak: Int, points: Int, totalCompletions: Int): List<String> {
        return badgeDefinitions(bestStreak, points, totalCompletions)
            .filter { it.unlocked }                  // Keep only unlocked badges
            .map { "${it.title} — ${it.description}" } // Format badge display text
    }

    // Returns the list of badges that are still locked
    fun lockedBadges(bestStreak: Int, points: Int, totalCompletions: Int): List<BadgeDefinition> {
        return badgeDefinitions(bestStreak, points, totalCompletions)
            .filterNot { it.unlocked }               // Keep only locked badges
    }

    // Provides a motivational message based on the user's best streak
    fun motivationMessage(bestStreak: Int): String {
        return when {
            bestStreak >= 30 -> "Legendary consistency! Keep shining."        // 30+ days message
            bestStreak >= 14 -> "Two weeks strong — amazing focus."           // 14+ days message
            bestStreak >= 7 -> "One week streak! Keep the rhythm."            // 7+ days message
            bestStreak >= 3 -> "Great start! You’re building momentum."       // 3+ days message
            bestStreak > 0 -> "Every win counts. Show up again tomorrow."     // 1–2 days message
            else -> "Time to start a new streak today!"                       // If no streak yet
        }
    }
}
