package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

//Data Access Object for Category-related database operations
class CategoryDao(private val dbHelper: BudgetDatabase) {
    private val TAG = "CategoryDao"

    //Inserts a new category
    fun insert(category: Category): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_CATEGORY_NAME, category.name)
            put(BudgetDatabase.COLUMN_CATEGORY_COLOR, category.color)
            put(BudgetDatabase.COLUMN_CATEGORY_BUDGET, category.budgetLimit)
            category.userId?.let { put(BudgetDatabase.COLUMN_CATEGORY_USER_ID, it) }
        }

        return try {
            val id = db.insert(BudgetDatabase.TABLE_CATEGORIES, null, values)
            if (id == -1L) {
                Log.e(TAG, "Failed to insert category: ${category.name}")
            }
            id
        } catch (e: Exception) {
            Log.e(TAG, "Exception while inserting category: ${e.message}")
            -1
        } finally {
            db.close()
        }
    }

    //Updates an existing category
    fun update(category: Category): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_CATEGORY_NAME, category.name)
            put(BudgetDatabase.COLUMN_CATEGORY_COLOR, category.color)
            put(BudgetDatabase.COLUMN_CATEGORY_BUDGET, category.budgetLimit)
            category.userId?.let { put(BudgetDatabase.COLUMN_CATEGORY_USER_ID, it) }
        }

        val selection = "${BudgetDatabase.COLUMN_CATEGORY_ID} = ?"
        val selectionArgs = arrayOf(category.id.toString())

        return try {
            val rowsAffected = db.update(
                BudgetDatabase.TABLE_CATEGORIES,
                values,
                selection,
                selectionArgs
            )
            if (rowsAffected == 0) {
                Log.e(TAG, "Failed to update category: ${category.name}")
            }
            rowsAffected
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating category: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    //Deletes a category
    fun delete(categoryId: Long): Int {
        val db = dbHelper.writableDatabase
        val selection = "${BudgetDatabase.COLUMN_CATEGORY_ID} = ?"
        val selectionArgs = arrayOf(categoryId.toString())

        return try {
            val rowsAffected = db.delete(BudgetDatabase.TABLE_CATEGORIES, selection, selectionArgs)
            rowsAffected
        } catch (e: Exception) {
            Log.e(TAG, "Exception while deleting category: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    //Gets all categories for a user
    fun getCategoriesForUser(userId: Long): List<Category> {
        val categories = mutableListOf<Category>()
        val db = dbHelper.readableDatabase

        // Gets both default categories (userId is null) and user-specific categories
        val selection = "${BudgetDatabase.COLUMN_CATEGORY_USER_ID} = ? OR ${BudgetDatabase.COLUMN_CATEGORY_USER_ID} IS NULL"
        val selectionArgs = arrayOf(userId.toString())

        var cursor: Cursor? = null

        try {
            cursor = db.query(
                BudgetDatabase.TABLE_CATEGORIES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "${BudgetDatabase.COLUMN_CATEGORY_NAME} ASC"
            )

            while (cursor?.moveToNext() == true) {
                val idIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_CATEGORY_ID)
                val nameIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_CATEGORY_NAME)
                val colorIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_CATEGORY_COLOR)
                val budgetIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_CATEGORY_BUDGET)
                val userIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_CATEGORY_USER_ID)

                // Checks if columns exist
                if (idIndex != -1 && nameIndex != -1 && colorIndex != -1 && budgetIndex != -1) {
                    val id = cursor.getLong(idIndex)
                    val name = cursor.getString(nameIndex)
                    val color = cursor.getString(colorIndex)
                    val budget = cursor.getDouble(budgetIndex)
                    val categoryUserId = if (userIdIndex != -1 && !cursor.isNull(userIdIndex)) {
                        cursor.getLong(userIdIndex)
                    } else {
                        null
                    }

                    categories.add(Category(id, name, color, budget, categoryUserId))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting categories: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return categories
    }
}
