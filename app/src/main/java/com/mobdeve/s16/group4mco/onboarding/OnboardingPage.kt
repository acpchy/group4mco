package com.mobdeve.s16.group4mco.onboarding

// Data class representing a single page in the onboarding flow
data class OnboardingPage(
    val title: String,                 // Main heading text for the onboarding page
    val subtitle: String,              // Smaller descriptive text under the title
    val chips: List<String> = emptyList(), // Optional list of selectable chip labels
    val showForm: Boolean = false          // Indicates if this page should display a form (e.g., email input)
)
