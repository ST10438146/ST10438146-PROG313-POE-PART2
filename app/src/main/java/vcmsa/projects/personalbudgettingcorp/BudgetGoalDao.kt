package vcmsa.projects.personalbudgettingcorp.vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import vcmsa.projects.personalbudgettingcorp.BudgetDatabase
import vcmsa.projects.personalbudgettingcorp.BudgetGoal
import android.content.Context
import com.google.common.reflect.TypeToken
import java.lang.reflect.Type

//Data Access Object for Budget-related database operations
class BudgetDao(context: Context) {
    private val dbHelper = BudgetDatabase(context)
    private val database = dbHelper.writableDatabase
    private val TAG = "BudgetDao"
    private val gson = Gson()

    // Error handling helper
    private fun <T> executeWithCatch(operation: () -> T?, errorMessage: String): T? {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e(TAG, "$errorMessage: ${e.message}")
            null
        }
    }

    //Inserts a new budget

    fun insert(budget: Budget): Long {
        val categoryLimitsJson = gson.toJson(budget.categoryLimits)
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_BUDGET_TOTAL, budget.totalBudget)
            put(BudgetDatabase.COLUMN_BUDGET_USER_ID, budget.userId)
            put(BudgetDatabase.COLUMN_BUDGET_CATEGORY_LIMITS, categoryLimitsJson)
        }

        return executeWithCatch({
            database.insert(BudgetDatabase.TABLE_BUDGETS, null, values)
        }, "Failed to insert budget") ?: -1
    }

    //Gets budget for a user

    fun getBudgetForUser(userId: Long): Budget? {
        var budget: Budget? = null
        val cursor = executeWithCatch({
            database.query(
                BudgetDatabase.TABLE_BUDGETS,
                null,
                "${BudgetDatabase.COLUMN_BUDGET_USER_ID} = ?",
                arrayOf(userId.toString()),
                null,
                null,
                null
            )
        }, "Failed to get budget for user")

        cursor?.use {
            if (it.moveToFirst()) {
                budget = mapCursorToBudget(it)
            }
        }
        return budget
    }

    //Updates an existing budget

    fun update(budget: Budget): Int {
        val categoryLimitsJson = gson.toJson(budget.categoryLimits)
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_BUDGET_TOTAL, budget.totalBudget)
            put(BudgetDatabase.COLUMN_BUDGET_CATEGORY_LIMITS, categoryLimitsJson)
        }

        val selection = "${BudgetDatabase.COLUMN_BUDGET_USER_ID} = ?"
        val selectionArgs = arrayOf(budget.userId.toString())

        return executeWithCatch({
            database.update(
                BudgetDatabase.TABLE_BUDGETS,
                values,
                selection,
                selectionArgs
            )
        }, "Failed to update budget") ?: 0
    }

    //Deletes budget for a user

    fun delete(userId: Long): Int {
        val selection = "${BudgetDatabase.COLUMN_BUDGET_USER_ID} = ?"
        val selectionArgs = arrayOf(userId.toString())

        return executeWithCatch({
            database.delete(BudgetDatabase.TABLE_BUDGETS, selection, selectionArgs)
        }, "Failed to delete budget") ?: 0
    }

    //Helper function to map a cursor to a Budget object
    private fun mapCursorToBudget(cursor: Cursor): Budget {
        val categoryLimitsJson = cursor.getString(cursor.getColumnIndexOrThrow(BudgetDatabase.COLUMN_BUDGET_CATEGORY_LIMITS))
        val type: Type = object : TypeToken<Map<Long, Double>>() {}.type
        val categoryLimits: Map<Long, Double> = gson.fromJson(categoryLimitsJson, type) ?: emptyMap()

        return Budget(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(BudgetDatabase.COLUMN_BUDGET_ID)),
            totalBudget = cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetDatabase.COLUMN_BUDGET_TOTAL)),
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(BudgetDatabase.COLUMN_BUDGET_USER_ID)),
            categoryLimits = categoryLimits
        )
    }

    //Closes the database connection.
    fun close() {
        database.close()
        dbHelper.close()
    }
}

