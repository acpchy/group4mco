package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group4mco.databinding.ActivityLoginBinding
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding  // View binding for login layout
    private lateinit var dbHelper: DatabaseHelper       // Helper for database operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPrefs = getSharedPreferences("UserSettings", MODE_PRIVATE) // SharedPreferences for storing login info
        dbHelper = DatabaseHelper(this)

        // Login button click listener
        binding.loginBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                // Show error if fields are empty
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            } else if (dbHelper.checkUser(email, password)) {
                // Save login info to SharedPreferences
                userPrefs.edit {
                    putString("LOGGED_IN_EMAIL", email)
                    putString("LOGGED_IN_USER_FIRSTNAME", dbHelper.getName(email))
                    putString("LOGGED_IN_USER_SURNAME", dbHelper.getSurname(email))
                    putBoolean("IS_LOGGED_IN", true)
                }
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DashboardActivity::class.java)) // Navigate to dashboard
                finish()
            } else {
                // Show error if credentials are incorrect
                Toast.makeText(this, "The supplied credentials are incorrect.", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to registration screen
        binding.toRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
