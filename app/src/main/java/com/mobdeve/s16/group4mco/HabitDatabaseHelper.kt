package com.mobdeve.s16.group4mco

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mobdeve.s16.group4mco.analytics.CategoryBreakdown
import com.mobdeve.s16.group4mco.analytics.TrendPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class HabitDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

     // Database constants including version, table names, and column names.
     // Two tables exist: one for habit definitions and one for completion logs.
    companion object {
        private const val DATABASE_NAME = "habit_tracker.db"
        private const val DATABASE_VERSION = 2

        // HABIT TABLE
        private const val TABLE_HABITS = "habits"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_CATEGORY = "category"
        private const val COL_DESCRIPTION = "description"
        private const val COL_FREQUENCY = "frequency"
        private const val COL_REMINDER = "reminder_time"

        // LOG TABLE
        private const val TABLE_LOGS = "habit_logs"
        private const val COL_LOG_ID = "log_id"
        private const val COL_HABIT_ID = "habit_id"
        private const val COL_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createHabitsTable = """
            CREATE TABLE $TABLE_HABITS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT,
                $COL_CATEGORY TEXT,
                $COL_DESCRIPTION TEXT,
                $COL_FREQUENCY TEXT,
                $COL_REMINDER TEXT
            );
        """.trimIndent()

        // Table for logging when a habit is completed (one row per completion date per habit)
        val createLogsTable = """
            CREATE TABLE $TABLE_LOGS (
                $COL_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_HABIT_ID INTEGER,
                $COL_DATE TEXT,
                FOREIGN KEY($COL_HABIT_ID) REFERENCES $TABLE_HABITS($COL_ID)
            );
        """.trimIndent()

        db.execSQL(createHabitsTable)
        db.execSQL(createLogsTable)

        insertDummyData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HABITS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        onCreate(db)
    }

    // Inserts initial sample data into the database.
    // Also generates random historical logs (past 5-14 days) for visualization purposes.
    private fun insertDummyData(db: SQLiteDatabase) {
        val habits = listOf(
            Habit(0, "Drink Water!", "Health", "Drink 8 glasses daily", "Daily", "08:00"),
            Habit(0, "Study for CCPROG1 Final Exam", "Study", "Study for 2 hours", "Daily", "18:00"),
            Habit(0, "Cardio Exercise", "Health", "30-minute exercise, 3 sets, 15-min run", "Weekdays", "14:30"),
            Habit(0, "Meditate & Pray", "Lifestyle", "Meditate for 10 mins and do a little bit of yoga", "Daily", "21:00"),
            Habit(0, "Clean your room!", "Lifestyle", "30-minute cleanup", "Weekends", "10:00"),
        )

        val habitIds = mutableListOf<Int>()

        for (habit in habits) {
            val cv = ContentValues().apply {
                put(COL_NAME, habit.name)
                put(COL_CATEGORY, habit.category)
                put(COL_DESCRIPTION, habit.description)
                put(COL_FREQUENCY, habit.frequency)
                put(COL_REMINDER, habit.reminderTime)
            }
            val id = db.insert(TABLE_HABITS, null, cv).toInt()
            habitIds.add(id)
        }

        // Populate logs (7–14 days back)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        for (habitId in habitIds) {
            val daysOfLogs = Random.nextInt(5, 14) // 5 to 14 days of logs

            for (i in 0 until daysOfLogs) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val date = sdf.format(calendar.time)

                // 70% chance log exists on that day
                if (Random.nextFloat() < 0.7f) {
                    val cv = ContentValues().apply {
                        put(COL_HABIT_ID, habitId)
                        put(COL_DATE, date)
                    }
                    db.insert(TABLE_LOGS, null, cv)
                }
            }
        }
    }

    // Add a habit
    fun insertHabit(habit: Habit): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_NAME, habit.name)
            put(COL_CATEGORY, habit.category)
            put(COL_DESCRIPTION, habit.description)
            put(COL_FREQUENCY, habit.frequency)
            put(COL_REMINDER, habit.reminderTime)
        }
        val id = db.insert(TABLE_HABITS, null, cv)
        db.close()
        return id
    }

    // Enumerates all habits
    fun getAllHabits(): List<Habit> {
        val list = mutableListOf<Habit>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_HABITS", null)

        if (cursor.moveToFirst()) {
            do {
                val habit = Habit(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                    frequency = cursor.getString(cursor.getColumnIndexOrThrow(COL_FREQUENCY)),
                    reminderTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_REMINDER))
                )
                list.add(habit)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }

    // Get the number of habits
    fun countHabits(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_HABITS", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    // Mark Habit as completed
    fun markCompleted(habitId: Int, date: String) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_HABIT_ID, habitId)
            put(COL_DATE, date)
        }
        db.insert(TABLE_LOGS, null, cv)
        db.close()
    }

    // 'Uncompletes' (Unchecks) a completed habit
    fun removeCompletion(habitId: Int, date: String) {
        val db = writableDatabase
        db.delete(
            TABLE_LOGS,
            "$COL_HABIT_ID = ? AND $COL_DATE = ?",
            arrayOf(habitId.toString(), date)
        )
        db.close()
    }

    // Get completion date
    fun isCompletedForDate(habitId: Int, date: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LOGS WHERE $COL_HABIT_ID = ? AND $COL_DATE = ?",
            arrayOf(habitId.toString(), date)
        )
        cursor.moveToFirst()
        val completed = cursor.getInt(0) > 0
        cursor.close()
        db.close()
        return completed
    }

    // Get number of completed activities
    fun countCompletedToday(): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(DISTINCT $COL_HABIT_ID) FROM $TABLE_LOGS WHERE $COL_DATE = ?",
            arrayOf(today)
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }


    // Calculates the current streak for a specific habit.
    // Checks consecutive days backwards starting from today.
    fun computeStreak(habitId: Int): Int {
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
                SELECT $COL_DATE 
                FROM $TABLE_LOGS 
                WHERE $COL_HABIT_ID = ?
                ORDER BY $COL_DATE DESC
            """.trimIndent(),
            arrayOf(habitId.toString())
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            db.close()
            return 0
        }

        var streak = 0
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        var expectedDate = formatter.format(calendar.time)

        do {
            val loggedDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))
            if (loggedDate == expectedDate) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                expectedDate = formatter.format(calendar.time)
            } else break
        } while (cursor.moveToNext())

        cursor.close()
        db.close()
        return streak
    }

    // Aggregates statistics for a single habit:
    // Total completions, Current streak, Longest streak, Last completed date.
    fun getHabitStats(habitId: Int): HabitStats {
        val db = readableDatabase

        // total completions
        val totalCursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LOGS WHERE $COL_HABIT_ID = ?",
            arrayOf(habitId.toString())
        )
        totalCursor.moveToFirst()
        val totalCompletions = totalCursor.getInt(0)
        totalCursor.close()

        // last completed date (most recent)
        val lastCursor = db.rawQuery(
            "SELECT $COL_DATE FROM $TABLE_LOGS WHERE $COL_HABIT_ID = ? ORDER BY $COL_DATE DESC LIMIT 1",
            arrayOf(habitId.toString())
        )
        val lastCompleted = if (lastCursor.moveToFirst()) lastCursor.getString(0) else null
        lastCursor.close()

        // fetch all dates ordered ascending to compute longest streak
        val datesCursor = db.rawQuery(
            "SELECT $COL_DATE FROM $TABLE_LOGS WHERE $COL_HABIT_ID = ? ORDER BY $COL_DATE ASC",
            arrayOf(habitId.toString())
        )

        var longestStreak = 0
        var currentStreak = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var prevDate: Date? = null

        if (datesCursor.moveToFirst()) {
            do {
                val dateStr = datesCursor.getString(0)
                val d = sdf.parse(dateStr)

                if (prevDate == null) {
                    currentStreak = 1
                } else {
                    val diffDays =
                        ((d!!.time - prevDate.time) / (1000 * 60 * 60 * 24)).toInt()
                    if (diffDays == 1) {
                        currentStreak++
                    } else {
                        longestStreak = maxOf(longestStreak, currentStreak)
                        currentStreak = 1
                    }
                }
                prevDate = d
            } while (datesCursor.moveToNext())
        }

        longestStreak = maxOf(longestStreak, currentStreak)

        datesCursor.close()
        db.close()

        return HabitStats(
            habitId = habitId,
            totalCompletions = totalCompletions,
            streak = computeStreak(habitId),
            longestStreak = longestStreak,
            lastCompleted = lastCompleted
        )
    }

    // Get the habit streak for a given range of days.
    fun calculateRangeStreak(rangeDays: Int): Int {
        val db = readableDatabase
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val startCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -(rangeDays - 1))
        }
        val startDate = formatter.format(startCalendar.time)

        val cursor = db.rawQuery(
            """
                SELECT DISTINCT $COL_DATE 
                FROM $TABLE_LOGS
                WHERE $COL_DATE >= ?
                ORDER BY $COL_DATE DESC
            """.trimIndent(),
            arrayOf(startDate)
        )

        var streak = 0
        var expectedDate = formatter.format(calendar.time)

        if (cursor.moveToFirst()) {
            do {
                val loggedDate = cursor.getString(0)
                if (loggedDate == expectedDate) {
                    streak++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    expectedDate = formatter.format(calendar.time)
                } else {
                    val expected = formatter.parse(expectedDate)
                    val logged = formatter.parse(loggedDate)
                    if (expected != null && logged != null) {
                        val diff = daysBetween(expected, logged)
                        if (diff > 0) break
                    }
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return streak
    }

    private fun daysBetween(later: Date, earlier: Date): Int {
        val diff = later.time - earlier.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    // Get completion trend of habits
    fun getCompletionTrend(daysBack: Int = 7): List<TrendPoint> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -(daysBack - 1))
        }

        val startDate = formatter.format(calendar.time)
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT $COL_DATE, COUNT(DISTINCT $COL_HABIT_ID) as total
                FROM $TABLE_LOGS
                WHERE $COL_DATE >= ?
                GROUP BY $COL_DATE
                ORDER BY $COL_DATE ASC
            """.trimIndent(),
            arrayOf(startDate)
        )

        val map = mutableMapOf<String, Int>()
        if (cursor.moveToFirst()) {
            do {
                map[cursor.getString(0)] = cursor.getInt(1)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        val points = mutableListOf<TrendPoint>()
        val rollingCalendar = Calendar.getInstance().apply {
            time = formatter.parse(startDate)!!
        }
        repeat(daysBack) {
            val dateKey = formatter.format(rollingCalendar.time)
            val value = map[dateKey] ?: 0
            points.add(
                TrendPoint(
                    label = labelFormatter.format(rollingCalendar.time),
                    value = value
                )
            )
            rollingCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return points
    }

    // Get the categorical breakdown of habits
    fun getCategoryBreakdown(): List<CategoryBreakdown> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT h.$COL_CATEGORY, COUNT(l.$COL_LOG_ID) 
                FROM $TABLE_HABITS h
                LEFT JOIN $TABLE_LOGS l ON h.$COL_ID = l.$COL_HABIT_ID
                GROUP BY h.$COL_CATEGORY
            """.trimIndent(),
            emptyArray()
        )

        val breakdown = mutableListOf<CategoryBreakdown>()
        if (cursor.moveToFirst()) {
            do {
                breakdown.add(
                    CategoryBreakdown(
                        category = cursor.getString(0) ?: "Uncategorized",
                        completions = cursor.getInt(1)
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return breakdown
    }

    // Get total completion points
    fun getTotalCompletionPoints(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LOGS",
            null
        )
        cursor.moveToFirst()
        val points = cursor.getInt(0) * 10 // 10 points per completion
        cursor.close()
        db.close()
        return points
    }

    // Updates an existing habit
    fun updateHabit(habit: Habit): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_NAME, habit.name)
            put(COL_CATEGORY, habit.category)
            put(COL_DESCRIPTION, habit.description)
            put(COL_FREQUENCY, habit.frequency)
            put(COL_REMINDER, habit.reminderTime)
        }
        return db.update(
            TABLE_HABITS,
            cv,
            "$COL_ID = ?",
            arrayOf(habit.id.toString())
        )
    }

    // Deletes an existing habit
    fun deleteHabit(habitId: Int): Int {
        val db = writableDatabase

        db.delete(TABLE_LOGS, "$COL_HABIT_ID = ?", arrayOf(habitId.toString()))

        return db.delete(TABLE_HABITS, "$COL_ID = ?", arrayOf(habitId.toString()))
    }

    // Enumerates a specific habit based on the habit ID.
    fun getHabitById(habitId: Int): Habit? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_HABITS WHERE $COL_ID = ?", arrayOf(habitId.toString()))

        var habit: Habit? = null
        if (cursor.moveToFirst()) {
            habit = Habit(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                frequency = cursor.getString(cursor.getColumnIndexOrThrow(COL_FREQUENCY)),
                reminderTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_REMINDER))
            )
        }
        cursor.close()
        db.close()
        return habit
    }
}
