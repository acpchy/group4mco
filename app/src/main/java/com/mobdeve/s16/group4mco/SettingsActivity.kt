package com.mobdeve.s16.group4mco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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

        val userPrefs = getSharedPreferences("UserSettings", MODE_PRIVATE)
        binding.settingsEmail.setText(userPrefs.getString("LOGGED_IN_EMAIL", null))
        binding.settingsName.setText(dbHelper.getName(userPrefs.getString("LOGGED_IN_EMAIL", null)))
        binding.settingSurname.setText(
            dbHelper.getSurname(
                userPrefs.getString(
                    "LOGGED_IN_EMAIL",
                    null
                )
            )
        )
        binding.appNotificationSwitch.isChecked = userPrefs.getBoolean(
            "SEND_NOTIFICATIONS",
            false)

        binding.appName.text = getString(R.string.app_name)
        binding.appVersionNumber.text = "Version ${getString(R.string.app_version)}"

        binding.saveSettingsBtn.setOnClickListener {
            val name = binding.settingsName.text.toString().trim()
            val email = binding.settingsEmail.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.updateUser(name, email)
                userPrefs.edit {
                    putString("LOGGED_IN_EMAIL", email)
                }
                Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            }
        }

        binding.logoutBtn.setOnClickListener {
            userPrefs.edit {
                clear()
            }
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

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
                }
            } else {
                userPrefs.edit { putBoolean("SEND_NOTIFICATIONS", isChecked) }
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
            }
        }
    }
}

