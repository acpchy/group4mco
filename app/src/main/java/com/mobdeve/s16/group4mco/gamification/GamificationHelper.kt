package com.mobdeve.s16.group4mco.gamification

object GamificationHelper {

    fun buildBadges(bestStreak: Int, points: Int, totalCompletions: Int): List<String> {
        val badges = mutableListOf<String>()

        if (totalCompletions >= 1) badges.add("First Step — Logged your first habit completion")
        if (bestStreak >= 3) badges.add("Warm-Up — 3-day streak")
        if (bestStreak >= 7) badges.add("7-Day Streak — One full week of momentum")
        if (bestStreak >= 30) badges.add("Monthly Master — 30 days of consistency")
        if (points >= 500) badges.add("Points Champion — 500+ motivation points")
        if (totalCompletions >= 50) badges.add("Milestone Maker — 50 total completions")

        return badges
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

