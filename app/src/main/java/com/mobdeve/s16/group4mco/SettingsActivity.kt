package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val nameField = findViewById<EditText>(R.id.settingsName)
        val emailField = findViewById<EditText>(R.id.settingsEmail)
        val themeSwitch = findViewById<Switch>(R.id.themeSwitch)
        val saveBtn = findViewById<Button>(R.id.saveSettingsBtn)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        saveBtn.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            }
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                Toast.makeText(this, "Dark Mode enabled", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Light Mode enabled", Toast.LENGTH_SHORT).show()
        }

        logoutBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

