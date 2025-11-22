package com.mobdeve.s16.group4mco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.mobdeve.s16.group4mco.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)

        // Load current user details and settings from SharedPreferences
        val userPrefs = getSharedPreferences("UserSettings", MODE_PRIVATE)
        binding.settingsEmail.setText(userPrefs.getString("LOGGED_IN_EMAIL", null))
        binding.settingsName.setText(userPrefs.getString("LOGGED_IN_USER_FIRSTNAME", null))
        binding.settingSurname.setText(userPrefs.getString("LOGGED_IN_USER_SURNAME", null)
        )
        binding.appNotificationSwitch.isChecked = userPrefs.getBoolean(
            "SEND_NOTIFICATIONS",
            false)
        binding.snoozeDuration.setText(userPrefs.getInt("SNOOZE_TIME", 5).toString())
        binding.snoozeDuration.isEnabled = userPrefs.getBoolean("SEND_NOTIFICATIONS", false)
        binding.changeSnoozeTimeButton.isEnabled = userPrefs.getBoolean("SEND_NOTIFICATIONS", false)

        // Load app name, version, and icon/logo
        binding.appName.text = getString(R.string.app_name)
        binding.appVersionNumber.text = "Version ${getString(R.string.app_version)}"
        binding.appLogo.setImageResource(R.drawable.consistify_icon)

        // Handle saving profile changes
        binding.saveSettingsBtn.setOnClickListener {
            val newFirstName = binding.settingsName.text.toString().trim()
            val newSurname = binding.settingSurname.text.toString().trim()
            val newEmail = binding.settingsEmail.text.toString().trim()

            val currentEmail = userPrefs.getString("LOGGED_IN_EMAIL", null) ?: ""

            if (newFirstName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Update SharedPreferences with new profile info
                userPrefs.edit {
                    putString("LOGGED_IN_EMAIL", newEmail)
                    putString("LOGGED_IN_USER_FIRSTNAME", newFirstName)
                    putString("LOGGED_IN_USER_SURNAME", newSurname)
                }
                // Update the user record in the SQLite database
                dbHelper.updateUser(currentEmail, newFirstName, newSurname, newEmail)
                Toast.makeText(this, "User Profile settings saved", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout logic: clear preferences and redirect to Login
        binding.logoutBtn.setOnClickListener {
            userPrefs.edit {
                clear()
            }
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Toggle notifications, requesting permission on Android 13+
        binding.appNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Android 13 now requires the user to grant the app permission to send notifications
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        101
                    )
                } else {
                    userPrefs.edit { putBoolean("SEND_NOTIFICATIONS", isChecked) }
                    binding.snoozeDuration.isEnabled = isChecked
                    binding.changeSnoozeTimeButton.isEnabled = isChecked
                }
            } else {
                userPrefs.edit { putBoolean("SEND_NOTIFICATIONS", isChecked) }
                binding.snoozeDuration.isEnabled = isChecked
                binding.changeSnoozeTimeButton.isEnabled = isChecked
            }
        }

        // Validate and save custom snooze duration
        binding.changeSnoozeTimeButton.setOnClickListener {
            val inputStr = binding.snoozeDuration.text.toString()
            val snoozeTime = inputStr.toIntOrNull()
            if (snoozeTime != null && snoozeTime in 1..10) {
                userPrefs.edit { putInt("SNOOZE_TIME", snoozeTime) }
                binding.snoozeDuration.error = null
                Toast.makeText(this, "Snooze time changed to $snoozeTime minute(s)", Toast.LENGTH_SHORT).show()
            } else {
                binding.snoozeDuration.error = "Please enter a time between 1 and 10 minutes"
            }
        }
    }

    // Set the "SEND_NOTIFICATIONS" SharedPreferences to true when the user presses "Allow"
    // when their Android device (at least running Android 13) informs that the app needs to
    // permission to send notifications
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            val userPrefs = getSharedPreferences("UserSettings", MODE_PRIVATE)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                userPrefs.edit { putBoolean("SEND_NOTIFICATIONS", true) }
                binding.snoozeDuration.isEnabled = true
                binding.changeSnoozeTimeButton.isEnabled = true
            }
        }
    }
}

