package com.itismob.s16.group4mco

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.itismob.s16.group4mco.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences to store user login info
        val userPrefs = getSharedPreferences("UserSettings", MODE_PRIVATE)

        // Database helper to access user data
        dbHelper = DatabaseHelper(this)

        // Registration button click listener
        binding.registerBtn.setOnClickListener {
            // Get user input from form fields
            val name = binding.regName.text.toString().trim()
            val surname = binding.regSurname.text.toString().trim()
            val email = binding.regEmail.text.toString().trim()
            val password = binding.regPassword.text.toString().trim()
            val confirmPassword = binding.regConfirmPassword.text.toString().trim()

            // Validate that all fields are filled
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if email is already registered
            if (dbHelper.checkEmailExists(email)) {
                Toast.makeText(this, "Email already registered!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Insert new user into database
            val success = dbHelper.insertUser(name, surname, email, password)


            if (success) {
                // Save user login info in SharedPreferences
                userPrefs.edit {
                    putString("LOGGED_IN_EMAIL", email)
                    putString("LOGGED_IN_USER_FIRSTNAME", dbHelper.getName(email))
                    putString("LOGGED_IN_USER_SURNAME", dbHelper.getSurname(email))
                    putBoolean("IS_LOGGED_IN", true)
                }

                // Show welcome message
                "Welcome! ${userPrefs.getString("LOGGED_IN_USER_FIRSTNAME", null)}"

                // Navigate to Dashboard
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                // Show error if registration failed
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to login activity if user clicks the login link
        binding.toLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
