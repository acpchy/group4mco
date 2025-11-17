package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group4mco.databinding.ActivityDashboardBinding
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var habitDb: HabitDatabaseHelper
    private var habits = listOf<Habit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habitDb = HabitDatabaseHelper(this)

        binding.tvGreeting.text = "Hi, User!"
        binding.tvMotivation.text = "Let's make habits together!"

        binding.tvDate.text = todayDate()

        binding.btnAddHabit.setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }

        binding.btnCheckProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }

        binding.navHome.setOnClickListener {
            // already home
        }
        binding.navProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
        binding.navAchievements.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }
        binding.navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadHabits()
    }

    private fun loadHabits() {
        binding.habitContainer.removeAllViews()
        habits = habitDb.getAllHabits()

        val today = todayDateDatabaseFormat()

        for (habit in habits) {
            val habitLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 12, 12, 12)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val cb = CheckBox(this).apply {
                text = habit.name
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                setOnCheckedChangeListener(null)
                isChecked = habitDb.isCompletedForDate(habit.id, today)

                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        habitDb.markCompleted(habit.id, today)
                    } else {
                        habitDb.removeCompletion(habit.id, today)
                    }
                }

                setOnLongClickListener {
                    val intent = Intent(this@DashboardActivity, EditHabitActivity::class.java)
                    intent.putExtra("habitId", habit.id)
                    startActivity(intent)
                    true
                }
            }

            val desc = TextView(this).apply {
                text = habit.description ?: ""
                textSize = 14f
                setPadding(8, 0, 8, 8)
            }

            habitLayout.addView(cb)
            habitLayout.addView(desc)

            binding.habitContainer.addView(habitLayout)
        }
    }

    private fun todayDate(): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun todayDateDatabaseFormat(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
