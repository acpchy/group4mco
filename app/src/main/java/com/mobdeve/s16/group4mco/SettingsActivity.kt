package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
        binding.settingSurname.setText(dbHelper.getSurname(userPrefs.getString("LOGGED_IN_EMAIL", null)))
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
    }
}

