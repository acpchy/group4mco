package com.itismob.s16.group4mco

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// SQLite helper class for managing the users database
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Database name and version
        private const val DATABASE_NAME = "UserDB.db"
        private const val DATABASE_VERSION = 1

        // Table and column names
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_SURNAME = "surname"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
    }

    // Called when database is first created
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_SURNAME TEXT,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)

        // Automatically insert dummy users when DB is created
        insertDummyOnFirstLaunch(db)
    }

    // Called when database version is upgraded
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // Check if dummy users already exist, then insert them
    private fun insertDummyOnFirstLaunch(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USERS", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        insertDummyUsers(db)
    }

    // Insert predefined dummy users into the database
    private fun insertDummyUsers(db: SQLiteDatabase) {
        val dummyUsers = listOf(
            Triple("John", "Doe", "john.doe@gmail.com"),
            Triple("Sarah", "Cruz", "sarah.cruz@gmail.com"),
            Triple("Marr", "Salcedo", "marr_salcedo@dlsu.edu.ph"),
            Triple("Angelica", "Simpao", "angelica_simpao@dlsu.edu.ph"),
            Triple("Pallavi", "Sowl", "pallavi.sowl@gmail.com")
        )

        for (user in dummyUsers) {

            // Prevent duplicate emails
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?",
                arrayOf(user.third)
            )
            cursor.moveToFirst()
            val exists = cursor.getInt(0) > 0
            cursor.close()

            if (!exists) {
                val values = ContentValues().apply {
                    put(COLUMN_NAME, user.first)
                    put(COLUMN_SURNAME, user.second)
                    put(COLUMN_EMAIL, user.third)
                    put(COLUMN_PASSWORD, "password123")
                }
                db.insert(TABLE_USERS, null, values)
            }
        }
    }

    // Insert a new user into the database
    fun insertUser(name: String, surname: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_SURNAME, surname)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }

        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L // Return true if insert successful
    }

    // Update an existing user's details based on email
    fun updateUser(email: String, newName: String, newSurname: String, newEmail: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, newName)
            put(COLUMN_SURNAME, newSurname)
            put(COLUMN_EMAIL, newEmail)
        }

        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()
        return result > 0 // Return true if update affected rows
    }

    // Check if a user exists with the given email and password
    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL=? AND $COLUMN_PASSWORD=?",
            arrayOf(email, password)
        )

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Check if a given email is already registered
    fun checkEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL=?",
            arrayOf(email)
        )

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Retrieve the first name of the user with the current session email
    fun getName(currentSessionEmail: String?): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_NAME FROM $TABLE_USERS WHERE $COLUMN_EMAIL=?",
            arrayOf(currentSessionEmail)
        )

        val name = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
        } else null

        cursor.close()
        db.close()
        return name
    }

    // Retrieve the surname of the user with the current session email
    fun getSurname(currentSessionEmail: String?): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_SURNAME FROM $TABLE_USERS WHERE $COLUMN_EMAIL=?",
            arrayOf(currentSessionEmail)
        )

        val surname = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SURNAME))
        } else null

        cursor.close()
        db.close()
        return surname
    }
}
