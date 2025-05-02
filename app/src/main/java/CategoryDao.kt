package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import kotlin.coroutines.jvm.internal.CompletedContinuation.context

class CategoryDao(private  val dbHelper: BudgetDatabase) {

    private val dbHelper = BudgetDatabase(context)
    private val database = dbHelper.writableDatabase
    private val TAG = "CategoryDao"

    private fun <T> executeWithCatch(operation: () -> T?, errorMessage: String): T? {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e(TAG, "$errorMessage: ${e.message}")
            null
        }
    }

    //Retrieves all categories for a specific user.

    fun getCategoriesForUser(userId: Long): List<Category> {
        val categories = mutableListOf<Category>()
        val query = "SELECT * FROM ${BudgetDatabase.TABLE_CATEGORIES} WHERE ${BudgetDatabase.COLUMN_CATEGORY_USER_ID} = ?"
        val selectionArgs = arrayOf(userId.toString())

        executeWithCatch({
            val cursor: Cursor? = database.rawQuery(query, selectionArgs)
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val id = it.getLong(it.getColumnIndexOrThrow(BudgetDatabase.COLUMN_CATEGORY_ID))
                        val name = it.getString(it.getColumnIndexOrThrow(BudgetDatabase.COLUMN_CATEGORY_NAME))
                        val userld = it.getLong(it.getColumnIndexOrThrow(BudgetDatabase.COLUMN_CATEGORY_USER_ID))
                        val category = Category(id = id, name = name, userld = userld)
                        categories.add(category)
                    } while (it.moveToNext())
                }
            }
            categories
        }, "Failed to get categories for user")

        return categories
    }



    fun close() {
        database.close()
        dbHelper.close()
    }
}