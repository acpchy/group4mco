package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import com.mobdeve.s16.group4mco.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGetStarted.setOnClickListener {
            navigateToOnboarding()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToOnboarding()
        }, 2000)
    }

    private fun navigateToOnboarding() {
        val appSettingsPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val userPrefs = getSharedPreferences("UserSettings", MODE_PRIVATE)
        val isFirstTime = appSettingsPrefs.getBoolean("IS_FIRST_TIME", true)
        val isLoggedIn = userPrefs.getBoolean("IS_LOGGED_IN", false)

        if (isFirstTime) {
            appSettingsPrefs.edit { putBoolean("IS_FIRST_TIME", false) }
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            if(isLoggedIn) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        finish()
    }
}