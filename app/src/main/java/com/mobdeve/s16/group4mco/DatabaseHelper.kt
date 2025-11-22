package com.mobdeve.s16.group4mco

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDB.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_SURNAME = "surname"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
    }

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

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun insertDummyOnFirstLaunch(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USERS", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        insertDummyUsers(db)
    }

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
        return result != -1L
    }

    fun updateUser(email: String, newName: String, newSurname: String, newEmail: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, newName)
            put(COLUMN_SURNAME, newSurname)
            put(COLUMN_EMAIL, newEmail)
        }

        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()
        return result > 0
    }

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
