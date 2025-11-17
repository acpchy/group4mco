package com.mobdeve.s16.group4mco.gamification

data class BadgeDefinition(
    val title: String,
    val description: String,
    val unlocked: Boolean
)

object GamificationHelper {

    fun badgeDefinitions(bestStreak: Int, points: Int, totalCompletions: Int): List<BadgeDefinition> {
        val definitions = listOf(
            BadgeDefinition(
                "First Step",
                "Log your first habit completion",
                totalCompletions >= 1
            ),
            BadgeDefinition(
                "Warm-Up",
                "Hold a 3-day streak",
                bestStreak >= 3
            ),
            BadgeDefinition(
                "7-Day Streak",
                "Complete habits for 7 days straight",
                bestStreak >= 7
            ),
            BadgeDefinition(
                "Monthly Master",
                "Stay consistent for 30 consecutive days",
                bestStreak >= 30
            ),
            BadgeDefinition(
                "Points Champion",
                "Earn 500 motivation points",
                points >= 500
            ),
            BadgeDefinition(
                "Milestone Maker",
                "Reach 50 total completions",
                totalCompletions >= 50
            )
        )
        return definitions
    }

    fun buildBadges(bestStreak: Int, points: Int, totalCompletions: Int): List<String> {
        return badgeDefinitions(bestStreak, points, totalCompletions)
            .filter { it.unlocked }
            .map { "${it.title} — ${it.description}" }
    }

    fun lockedBadges(bestStreak: Int, points: Int, totalCompletions: Int): List<BadgeDefinition> {
        return badgeDefinitions(bestStreak, points, totalCompletions)
            .filterNot { it.unlocked }
    }

    fun motivationMessage(bestStreak: Int): String {
        return when {
            bestStreak >= 30 -> "Legendary consistency! Keep shining."
            bestStreak >= 14 -> "Two weeks strong — amazing focus."
            bestStreak >= 7 -> "One week streak! Keep the rhythm."
            bestStreak >= 3 -> "Great start! You’re building momentum."
            bestStreak > 0 -> "Every win counts. Show up again tomorrow."
            else -> "Time to start a new streak today!"
        }
    }
}

