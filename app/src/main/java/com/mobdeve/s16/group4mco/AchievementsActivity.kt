package com.mobdeve.s16.group4mco

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.mobdeve.s16.group4mco.databinding.ActivityAchievementsBinding

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sampleAchievements = listOf(
            "🎯 Started your first habit!",
            "🔥 3-day streak achieved!",
            "🌿 Completed 10 tasks total!"
        )

        val margin = resources.getDimensionPixelSize(R.dimen.spacing_3)
        val padding = resources.getDimensionPixelSize(R.dimen.card_padding)
        val cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
        val elevation = resources.getDimension(R.dimen.card_elevation)

        for (ach in sampleAchievements) {
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, margin / 2, 0, margin / 2)
                }
                radius = cornerRadius
                cardElevation = elevation
                setContentPadding(padding, padding, padding, padding)
            }

            val tv = TextView(this).apply {
                text = ach
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
            }

            card.addView(tv)
            binding.achievementsList.addView(card)
        }
    }
}
