package com.mobdeve.s16.group4mco.analytics

data class TrendPoint(
    val label: String,
    val value: Int
)

data class CategoryBreakdown(
    val category: String,
    val completions: Int
)

