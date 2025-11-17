package com.mobdeve.s16.group4mco

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group4mco.databinding.ActivityAchievementsBinding

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val achievementsList = findViewById<LinearLayout>(R.id.achievementsList)

        val sampleAchievements = listOf(
            "🎯 Started your first habit!",
            "🔥 3-day streak achieved!",
            "🌿 Completed 10 tasks total!"
        )

        for (ach in sampleAchievements) {
            val tv = TextView(this)
            tv.text = ach
            tv.textSize = 16f
            tv.setTextColor(android.graphics.Color.parseColor("#2B2B2B"))
            tv.setPadding(0, 12, 0, 12)
            binding.achievementsList.addView(tv)
        }
    }
}
