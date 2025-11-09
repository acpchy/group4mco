package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val nameInput = findViewById<EditText>(R.id.regName)
        val emailInput = findViewById<EditText>(R.id.regEmail)
        val passwordInput = findViewById<EditText>(R.id.regPassword)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val toLogin = findViewById<TextView>(R.id.toLogin)

        registerBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (dbHelper.checkEmailExists(email)) {
                Toast.makeText(this, "Email already registered!", Toast.LENGTH_SHORT).show()
            } else {
                val success = dbHelper.insertUser(name, email, password)
                if (success) {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        toLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
