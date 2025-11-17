package com.mobdeve.s16.group4mco.onboarding

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val chips: List<String> = emptyList(),
    val showForm: Boolean = false
)


