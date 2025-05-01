package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class CategoryDao(private val dbHelper: BudgetDatabase) {

    private val database: SQLiteDatabase = dbHelper.writableDatabase

    private fun <T> executeWithCatch(operation: () -> T?, errorMessage: String): T? {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e("CategoryDao", "$errorMessage: ${e.message}")
            null
        }
    }

    fun addCategory(categoryName: String): Long {
        val values = ContentValues().apply {
            put(BudgetDatabase.KEY_CATEGORY_NAME, categoryName)
        }
        return executeWithCatch({
            database.insert(BudgetDatabase.TABLE_CATEGORIES, null, values)
        }, "Error adding category") ?: -1
    }

    fun getAllCategories(): Cursor? {
        return executeWithCatch({
            database.query(
                BudgetDatabase.TABLE_CATEGORIES,
                arrayOf(BudgetDatabase.KEY_ID, BudgetDatabase.KEY_CATEGORY_NAME),
                null,
                null,
                null,
                null,
                null
            )
        }, "Error getting all categories")
    }

    fun getCategoryNameById(categoryId: Long): String? {
        var categoryName: String? = null
        val cursor = executeWithCatch({
            database.query(
                BudgetDatabase.TABLE_CATEGORIES,
                arrayOf(BudgetDatabase.KEY_CATEGORY_NAME),
                "${BudgetDatabase.KEY_ID} = ?",
                arrayOf(categoryId.toString()),
                null,
                null,
                null
            )
        }, "Error getting category name by ID")

        cursor?.use {
            if (it.moveToFirst()) {
                categoryName = it.getString(it.getColumnIndexOrThrow(BudgetDatabase.KEY_CATEGORY_NAME))
            }
        }
        return categoryName
    }

    fun close() {
        database.close()
    }
}