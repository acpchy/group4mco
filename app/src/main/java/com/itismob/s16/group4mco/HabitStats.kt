package com.itismob.s16.group4mco

data class HabitStats(
    val habitId: Int,
    val totalCompletions: Int,
    val streak: Int,
    val longestStreak: Int,
    val lastCompleted: String?
)
