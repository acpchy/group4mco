package com.mobdeve.s16.group4mco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.mobdeve.s16.group4mco.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding  // View binding for main layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // "Get Started" button click listener
        binding.btnGetStarted.setOnClickListener {
            navigateToOnboarding()  // Navigate to onboarding screen
        }

        // Automatically navigate after 2 seconds (splash effect)
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToOnboarding()
        }, 2000)
    }

    private fun navigateToOnboarding() {
        val appSettingsPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE) // App-level settings
        val userPrefs = getSharedPreferences("UserSettings", MODE_PRIVATE)        // User login settings
        val isFirstTime = appSettingsPrefs.getBoolean("IS_FIRST_TIME", true)     // Check if first app launch
        val isLoggedIn = userPrefs.getBoolean("IS_LOGGED_IN", false)            // Check if user is logged in

        if (isFirstTime) {
            // Mark that the app has been launched before
            appSettingsPrefs.edit { putBoolean("IS_FIRST_TIME", false) }
            startActivity(Intent(this, OnboardingActivity::class.java))  // Show onboarding screen
        } else {
            if (isLoggedIn) {
                startActivity(Intent(this, DashboardActivity::class.java))  // Go to dashboard
            } else {
                startActivity(Intent(this, LoginActivity::class.java))      // Go to login
            }
        }
        finish()  // Close MainActivity so it doesn't remain in back stack
    }
}
