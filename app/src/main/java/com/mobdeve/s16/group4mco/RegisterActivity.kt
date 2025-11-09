package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val name = findViewById<EditText>(R.id.regName)
        val email = findViewById<EditText>(R.id.regEmail)
        val pass = findViewById<EditText>(R.id.regPassword)
        val btn = findViewById<Button>(R.id.registerBtn)
        val toLogin = findViewById<TextView>(R.id.toLogin)

        btn.setOnClickListener {
            val n = name.text.toString().trim()
            val e = email.text.toString().trim()
            val p = pass.text.toString().trim()

            if (n.isEmpty() || e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Account created! Please sign in.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        toLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
