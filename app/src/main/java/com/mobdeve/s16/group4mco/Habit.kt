package com.mobdeve.s16.group4mco
data class Habit(
    val id: Int = 0,
    val name: String,
    val category: String,
    val description: String,
    val frequency: String,
    val reminderTime: String   // HH:mm
)
