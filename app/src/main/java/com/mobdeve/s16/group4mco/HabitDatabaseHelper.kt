package com.mobdeve.s16.group4mco

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class HabitDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HABITS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        onCreate(db)
    }

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

    fun countHabits(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_HABITS", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun markCompleted(habitId: Int, date: String) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_HABIT_ID, habitId)
            put(COL_DATE, date)
        }
        db.insert(TABLE_LOGS, null, cv)
        db.close()
    }

    fun removeCompletion(habitId: Int, date: String) {
        val db = writableDatabase
        db.delete(
            TABLE_LOGS,
            "$COL_HABIT_ID = ? AND $COL_DATE = ?",
            arrayOf(habitId.toString(), date)
        )
        db.close()
    }

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

    fun countCompletedToday(): Int {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(
            java.util.Date()
        )
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
        val calendar = java.util.Calendar.getInstance()
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        var expectedDate = formatter.format(calendar.time)

        do {
            val loggedDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))
            if (loggedDate == expectedDate) {
                streak++
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                expectedDate = formatter.format(calendar.time)
            } else break
        } while (cursor.moveToNext())

        cursor.close()
        db.close()
        return streak
    }

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
                    val diffDays = ((d!!.time - prevDate.time) / (1000 * 60 * 60 * 24)).toInt()
                    if (diffDays == 1) {
                        currentStreak++
                    } else {
                        if (currentStreak > longestStreak) longestStreak = currentStreak
                        currentStreak = 1
                    }
                }
                prevDate = d
            } while (datesCursor.moveToNext())
        }

        if (currentStreak > longestStreak) longestStreak = currentStreak
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

    fun deleteHabit(habitId: Int): Int {
        val db = writableDatabase

        db.delete(TABLE_LOGS, "$COL_HABIT_ID = ?", arrayOf(habitId.toString()))

        return db.delete(TABLE_HABITS, "$COL_ID = ?", arrayOf(habitId.toString()))
    }
}
