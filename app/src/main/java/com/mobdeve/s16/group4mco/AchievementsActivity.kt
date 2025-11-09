package com.mobdeve.s16.group4mco

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AchievementsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

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
            achievementsList.addView(tv)
        }
    }
}
