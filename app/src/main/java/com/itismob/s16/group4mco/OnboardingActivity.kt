package com.itismob.s16.group4mco

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.itismob.s16.group4mco.databinding.ActivityOnboardingBinding
import com.itismob.s16.group4mco.onboarding.OnboardingAdapter
import com.itismob.s16.group4mco.onboarding.OnboardingPage

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter

    // List of onboarding pages to display
    private val pages = listOf(
        OnboardingPage(
            title = "Create\nGood Habits",
            subtitle = "Change your life by slowly adding new healthy habits and sticking to them.",
            chips = listOf("Drink water", "Walk", "Meditate", "Read"),
            showForm = false
        ),
        OnboardingPage(
            title = "Track\nYour Progress",
            subtitle = "Every day you become one step closer to your goal. Don't give up!",
            chips = listOf("Jogging • 30 min", "Push ups • 20x", "Walking • 1.5km"),
            showForm = false
        ),
        OnboardingPage(
            title = "\nWelcome to Consistify",
            subtitle = "Let's get started. You will be asked to enter your credentials before proceeding.",
            showForm = false
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the ViewPager2 adapter with onboarding pages
        adapter = OnboardingAdapter(pages)
        binding.onboardingPager.adapter = adapter

        // Connect the TabLayout indicator with the ViewPager2
        TabLayoutMediator(binding.indicator, binding.onboardingPager) { _, _ -> }.attach()

        // Primary CTA button click listener
        binding.primaryCta.setOnClickListener {
            if (binding.onboardingPager.currentItem < pages.lastIndex) {
                // Go to the next page
                binding.onboardingPager.currentItem = binding.onboardingPager.currentItem + 1
            } else {
                // If last page, navigate to Login screen
                goToLogin()
            }
        }

        // Skip button click listener: directly go to last page
        binding.btnSkip.setOnClickListener {
            binding.onboardingPager.currentItem = pages.lastIndex
        }

        // Change primary CTA text based on current page
        binding.onboardingPager.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.primaryCta.text = if (position == pages.lastIndex) {
                    "Let's Get Started!" // Last page
                } else {
                    "Next >" // Other pages
                }
            }
        })
    }

    // Navigate to LoginActivity and finish onboarding
    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
