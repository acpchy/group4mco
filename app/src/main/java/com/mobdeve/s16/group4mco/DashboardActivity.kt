package com.mobdeve.s16.group4mco

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
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

        val margin = resources.getDimensionPixelSize(R.dimen.spacing_3)
        val padding = resources.getDimensionPixelSize(R.dimen.card_padding)
        val cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
        val elevation = resources.getDimension(R.dimen.card_elevation)
        val surfaceColor = ContextCompat.getColor(this, R.color.color_surface)
        val textSecondary = ContextCompat.getColor(this, R.color.color_on_surface_secondary)

        for ((index, habit) in habits.withIndex()) {
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, if (index == 0) margin else margin / 2, 0, margin)
                }
                radius = cornerRadius
                cardElevation = elevation
                setCardBackgroundColor(surfaceColor)
                setContentPadding(padding, padding, padding, padding)
            }

            val habitLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val cb = CheckBox(this).apply {
                text = habit.name
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
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
                setTextColor(textSecondary)
                setPadding(8, 0, 8, 8)
            }

            val actionsRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 0)
            }

            val editButton = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_edit)
                background = null
                setColorFilter(ContextCompat.getColor(context, R.color.color_primary))
                contentDescription = "Edit habit"
                setOnClickListener { launchEditHabit(habit.id) }
            }

            val deleteButton = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                background = null
                setColorFilter(ContextCompat.getColor(context, R.color.color_error))
                contentDescription = "Delete habit"
                setOnClickListener { confirmDeleteHabit(habit) }
            }

            actionsRow.addView(editButton)
            actionsRow.addView(deleteButton)

            habitLayout.addView(cb)
            habitLayout.addView(desc)
            habitLayout.addView(actionsRow)

            card.addView(habitLayout)
            binding.habitContainer.addView(card)
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

    private fun launchEditHabit(habitId: Int) {
        val intent = Intent(this, EditHabitActivity::class.java)
        intent.putExtra("habitId", habitId)
        startActivity(intent)
    }

    private fun confirmDeleteHabit(habit: Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete \"${habit.name}\"?")
            .setMessage("This will remove the habit and its history.")
            .setPositiveButton("Delete") { _, _ ->
                habitDb.deleteHabit(habit.id)
                loadHabits()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
