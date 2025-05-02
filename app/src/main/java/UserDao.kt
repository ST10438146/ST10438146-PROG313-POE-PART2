package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log

class UserDao(private val context: Context) {

    private val dbHelper = BudgetDatabase(context)
    private val database = dbHelper.writableDatabase

    companion object {
        private const val TAG = "UserDao"
    }

    // Error handling helper
    private fun <T> executeWithCatch(operation: () -> T?, errorMessage: String): T? {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e(TAG, "$errorMessage: ${e.message}")
            null
        }
    }

    //Adds a new user to the database.
    fun addUser(user: User): Long {
        val values = ContentValues().apply {
            put(BudgetDatabase.KEY_USERNAME, user.username)
            put(BudgetDatabase.KEY_PASSWORD, user.password) // WARNING: Insecure! Use hashing!
        }

        return executeWithCatch({
            database.insert(BudgetDatabase.TABLE_USERS, null, values)
        }, "Error adding user") ?: -1
    }

    //Retrieves a user from the database by their username.
    fun getUserByUsername(username: String): User? {
        var user: User? = null
        val cursor = executeWithCatch({
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

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(BudgetDatabase.KEY_ID))
                val storedUsername = it.getString(it.getColumnIndexOrThrow(BudgetDatabase.KEY_USERNAME))
                val storedPassword = it.getString(it.getColumnIndexOrThrow(BudgetDatabase.KEY_PASSWORD))
                user = User(id, storedUsername, storedPassword)
            }
        }
        return user
    }

    //Closes the database connection
    fun close() {
        database.close()
        dbHelper.close()
    }
}