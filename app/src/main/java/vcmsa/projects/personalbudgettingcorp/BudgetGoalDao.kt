package vcmsa.projects.personalbudgettingcorp.vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import vcmsa.projects.personalbudgettingcorp.BudgetDatabase
import vcmsa.projects.personalbudgettingcorp.BudgetGoal

//Data Access Object for BudgetGoal-related database operations
class BudgetGoalDao(private val dbHelper: BudgetDatabase) {
    private val TAG = "BudgetGoalDao"

    //Inserts a new budget goal
    fun insert(budgetGoal: BudgetGoal): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_BUDGET_AMOUNT, budgetGoal.amount)
            put(BudgetDatabase.COLUMN_BUDGET_MONTH, budgetGoal.month)
            put(BudgetDatabase.COLUMN_BUDGET_YEAR, budgetGoal.year)
            put(BudgetDatabase.COLUMN_BUDGET_USER_ID, budgetGoal.userId)
        }

        return try {
            val id = db.insert(BudgetDatabase.TABLE_BUDGET_GOALS, null, values)
            if (id == -1L) {
                Log.e(TAG, "Failed to insert budget goal")
            }
            id
        } catch (e: Exception) {
            Log.e(TAG, "Exception while inserting budget goal: ${e.message}")
            -1
        } finally {
            db.close()
        }
    }

    //Updates an existing budget goal
    fun update(budgetGoal: BudgetGoal): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_BUDGET_AMOUNT, budgetGoal.amount)
        }

        val selection = """
            ${BudgetDatabase.COLUMN_BUDGET_USER_ID} = ? AND
            ${BudgetDatabase.COLUMN_BUDGET_MONTH} = ? AND
            ${BudgetDatabase.COLUMN_BUDGET_YEAR} = ?
        """.trimIndent()

        val selectionArgs = arrayOf(
            budgetGoal.userId.toString(),
            budgetGoal.month.toString(),
            budgetGoal.year.toString()
        )

        return try {
            val rowsAffected = db.update(
                BudgetDatabase.TABLE_BUDGET_GOALS,
                values,
                selection,
                selectionArgs
            )

            // If no existing record to update, inserts a new one
            if (rowsAffected == 0) {
                insert(budgetGoal)
                return 1
            }

            rowsAffected
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating budget goal: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    //Gets a budget goal for a specific month and year
    fun getBudgetGoal(userId: Long, month: Int, year: Int): BudgetGoal? {
        val db = dbHelper.readableDatabase

        val selection = """
            ${BudgetDatabase.COLUMN_BUDGET_USER_ID} = ? AND
            ${BudgetDatabase.COLUMN_BUDGET_MONTH} = ? AND
            ${BudgetDatabase.COLUMN_BUDGET_YEAR} = ?
        """.trimIndent()

        val selectionArgs = arrayOf(userId.toString(), month.toString(), year.toString())

        var budgetGoal: BudgetGoal? = null
        var cursor: Cursor? = null

        try {
            cursor = db.query(
                BudgetDatabase.TABLE_BUDGET_GOALS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            )

            if (cursor?.moveToFirst() == true) {
                val idIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_BUDGET_ID)
                val amountIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_BUDGET_AMOUNT)

                // Checks if columns exist
                if (idIndex != -1 && amountIndex != -1) {
                    val id = cursor.getLong(idIndex)
                    val amount = cursor.getDouble(amountIndex)

                    budgetGoal = BudgetGoal(id, amount, month, year, userId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting budget goal: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return budgetGoal
    }
}
