package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

//Data Access Object for User-related database operations

class UserDao(private val dbHelper: BudgetDatabase) {
    private val TAG = "UserDao"

     //Inserts a new user into the database
     //returns The ID of the inserted user, or -1 if insertion failed
    fun insert(user: User): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_USER_USERNAME, user.username)
            put(BudgetDatabase.COLUMN_USER_PASSWORD, user.password)
        }

        return try {
            val id = db.insert(BudgetDatabase.TABLE_USERS, null, values)
            if (id == -1L) {
                Log.e(TAG, "Failed to insert user: ${user.username}")
            }
            id
        } catch (e: Exception) {
            Log.e(TAG, "Exception while inserting user: ${e.message}")
            -1
        } finally {
            db.close()
        }
    }
    //Gets a user by username
    fun getUserByUsername(username: String): User? {
        val db = dbHelper.readableDatabase
        val selection = "${BudgetDatabase.COLUMN_USER_USERNAME} = ?"
        val selectionArgs = arrayOf(username)

        var user: User? = null
        var cursor: Cursor? = null

        try {
            cursor = db.query(
                BudgetDatabase.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            )

            if (cursor?.moveToFirst() == true) {
                val idIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_USER_ID)
                val usernameIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_USER_USERNAME)
                val passwordIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_USER_PASSWORD)

                // Check if columns exist
                if (idIndex != -1 && usernameIndex != -1 && passwordIndex != -1) {
                    user = User(
                        id = cursor.getLong(idIndex),
                        username = cursor.getString(usernameIndex),
                        password = cursor.getString(passwordIndex)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting user: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return user
    }
    //Authenticates a user
    fun authenticate(username: String, password: String): User? {
        val user = getUserByUsername(username)
        return if (user != null && user.password == password) user else null
    }
}