package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class UserDao(private val dbHelper: BudgetDatabase) {

    private val database: SQLiteDatabase = dbHelper.writableDatabase

    // Error handling
    private fun <T> executeWithCatch(operation: () -> T?, errorMessage: String): T? {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e("UserDao", "$errorMessage: ${e.message}")
            null
        }
    }

    fun addUser(username: String, password: String): Long {
        val values = ContentValues().apply {
            put(BudgetDatabase.KEY_USERNAME, username)
            put(BudgetDatabase.KEY_PASSWORD, password)
        }
        return executeWithCatch({
            database.insert(BudgetDatabase.TABLE_USERS, null, values)
        }, "Error adding user") ?: -1
    }

    fun getUserByUsername(username: String): Cursor? {
        return executeWithCatch({
            database.query(
                BudgetDatabase.TABLE_USERS,
                arrayOf(BudgetDatabase.KEY_ID, BudgetDatabase.KEY_USERNAME, BudgetDatabase.KEY_PASSWORD),
                "${BudgetDatabase.KEY_USERNAME} = ?",
                arrayOf(username),
                null,
                null,
                null
            )
        }, "Error getting user by username")
    }

    // Closes the database connection
    fun close() {
        database.close()
    }
}