package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.icu.text.SimpleDateFormat
import android.util.Log
import java.util.Locale

//Data Access Object for Expense-related database operations
class ExpenseDao(private val dbHelper: BudgetDatabase) {
    private val TAG = "ExpenseDao"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    //Inserts a new expense
    fun insert(expense: Expense): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_EXPENSE_AMOUNT, expense.amount)
            put(BudgetDatabase.COLUMN_EXPENSE_DATE, expense.date)
            put(BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION, expense.description)
            put(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID, expense.categoryId)
            put(BudgetDatabase.COLUMN_EXPENSE_USER_ID, expense.userId)
            expense.receiptPath?.let { put(BudgetDatabase.COLUMN_EXPENSE_RECEIPT_PATH, it) }
        }

        return try {
            val id = db.insert(BudgetDatabase.TABLE_EXPENSES, null, values)
            if (id == -1L) {
                Log.e(TAG, "Failed to insert expense: ${expense.description}")
            }
            id
        } catch (e: Exception) {
            Log.e(TAG, "Exception while inserting expense: ${e.message}")
            -1
        } finally {
            db.close()
        }
    }

    //Updates an existing expense
    fun update(expense: Expense): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_EXPENSE_AMOUNT, expense.amount)
            put(BudgetDatabase.COLUMN_EXPENSE_DATE, expense.date)
            put(BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION, expense.description)
            put(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID, expense.categoryId)
            expense.receiptPath?.let { put(BudgetDatabase.COLUMN_EXPENSE_RECEIPT_PATH, it) }
        }

        val selection = "${BudgetDatabase.COLUMN_EXPENSE_ID} = ?"
        val selectionArgs = arrayOf(expense.id.toString())

        return try {
            val rowsAffected = db.update(
                BudgetDatabase.TABLE_EXPENSES,
                values,
                selection,
                selectionArgs
            )
            if (rowsAffected == 0) {
                Log.e(TAG, "Failed to update expense: ${expense.description}")
            }
            rowsAffected
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating expense: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    //Deletes an expense
    fun delete(expenseId: Long): Int {
        val db = dbHelper.writableDatabase
        val selection = "${BudgetDatabase.COLUMN_EXPENSE_ID} = ?"
        val selectionArgs = arrayOf(expenseId.toString())

        return try {
            val rowsAffected = db.delete(BudgetDatabase.TABLE_EXPENSES, selection, selectionArgs)
            rowsAffected
        } catch (e: Exception) {
            Log.e(TAG, "Exception while deleting expense: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    //Gets all expenses for a user within a date range
    fun getExpensesForUserInDateRange(userId: Long, startDate: String, endDate: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = dbHelper.readableDatabase

        val selection = """
            ${BudgetDatabase.COLUMN_EXPENSE_USER_ID} = ? AND 
            ${BudgetDatabase.COLUMN_EXPENSE_DATE} >= ? AND
            ${BudgetDatabase.COLUMN_EXPENSE_DATE} <= ?
        """.trimIndent()

        val selectionArgs = arrayOf(userId.toString(), startDate, endDate)

        var cursor: Cursor? = null

        try {
            cursor = db.query(
                BudgetDatabase.TABLE_EXPENSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "${BudgetDatabase.COLUMN_EXPENSE_DATE} DESC"
            )

            while (cursor?.moveToNext() == true) {
                val idIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_ID)
                val amountIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_AMOUNT)
                val dateIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_DATE)
                val descriptionIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION)
                val categoryIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID)
                val userIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_USER_ID)
                val receiptPathIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_RECEIPT_PATH)

                val expense = Expense(
                    id = cursor.getLong(idIndex),
                    amount = cursor.getDouble(amountIndex),
                    date = cursor.getString(dateIndex),
                    description = cursor.getString(descriptionIndex),
                    categoryId = cursor.getLong(categoryIdIndex),
                    userId = cursor.getLong(userIdIndex),
                    receiptPath = if (receiptPathIndex != -1 && !cursor.isNull(receiptPathIndex))
                        cursor.getString(receiptPathIndex) else null
                )
                expenses.add(expense)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting expenses: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return expenses
    }
    //Gets a single expense by ID
    fun getExpenseById(expenseId: Long): Expense? {
        val db = dbHelper.readableDatabase
        val selection = "${BudgetDatabase.COLUMN_EXPENSE_ID} = ?"
        val selectionArgs = arrayOf(expenseId.toString())
        var cursor: Cursor? = null
        var expense: Expense? = null

        try {
            cursor = db.query(
                BudgetDatabase.TABLE_EXPENSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            )

            if (cursor?.moveToFirst() == true) {
                val idIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_ID)
                val amountIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_AMOUNT)
                val dateIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_DATE)
                val descriptionIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION)
                val categoryIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID)
                val userIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_USER_ID)
                val receiptPathIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_RECEIPT_PATH)

                expense = Expense(
                    id = cursor.getLong(idIndex),
                    amount = cursor.getDouble(amountIndex),
                    date = cursor.getString(dateIndex),
                    description = cursor.getString(descriptionIndex),
                    categoryId = cursor.getLong(categoryIdIndex),
                    userId = cursor.getLong(userIdIndex),
                    receiptPath = if (receiptPathIndex != -1 && !cursor.isNull(receiptPathIndex))
                        cursor.getString(receiptPathIndex) else null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting expense by ID: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return expense
    }
    //Gets all expenses for a specific category in a date range
    fun getExpensesByCategory(userId: Long, categoryId: Long, startDate: String, endDate: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = dbHelper.readableDatabase

        val selection = """
            ${BudgetDatabase.COLUMN_EXPENSE_USER_ID} = ? AND 
            ${BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID} = ? AND
            ${BudgetDatabase.COLUMN_EXPENSE_DATE} >= ? AND
            ${BudgetDatabase.COLUMN_EXPENSE_DATE} <= ?
        """.trimIndent()

        val selectionArgs = arrayOf(userId.toString(), categoryId.toString(), startDate, endDate)

        var cursor: Cursor? = null

        try {
            cursor = db.query(
                BudgetDatabase.TABLE_EXPENSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "${BudgetDatabase.COLUMN_EXPENSE_DATE} DESC"
            )

            while (cursor?.moveToNext() == true) {
                val idIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_ID)
                val amountIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_AMOUNT)
                val dateIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_DATE)
                val descriptionIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION)
                val categoryIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID)
                val userIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_USER_ID)
                val receiptPathIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_RECEIPT_PATH)

                val expense = Expense(
                    id = cursor.getLong(idIndex),
                    amount = cursor.getDouble(amountIndex),
                    date = cursor.getString(dateIndex),
                    description = cursor.getString(descriptionIndex),
                    categoryId = cursor.getLong(categoryIdIndex),
                    userId = cursor.getLong(userIdIndex),
                    receiptPath = if (receiptPathIndex != -1 && !cursor.isNull(receiptPathIndex))
                        cursor.getString(receiptPathIndex) else null
                )
                expenses.add(expense)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting expenses by category: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return expenses
    }
    //Gets total expenses for a user in a specific time period
    fun getTotalExpensesByPeriod(userId: Long, startDate: String, endDate: String): Double {
        val db = dbHelper.readableDatabase
        var total = 0.0

        val selection = """
            ${BudgetDatabase.COLUMN_EXPENSE_USER_ID} = ? AND 
            ${BudgetDatabase.COLUMN_EXPENSE_DATE} >= ? AND
            ${BudgetDatabase.COLUMN_EXPENSE_DATE} <= ?
        """.trimIndent()

        val selectionArgs = arrayOf(userId.toString(), startDate, endDate)

        val query = """
            SELECT SUM(${BudgetDatabase.COLUMN_EXPENSE_AMOUNT}) as total 
            FROM ${BudgetDatabase.TABLE_EXPENSES}
            WHERE ${selection}
        """.trimIndent()

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(query, selectionArgs)

            if (cursor?.moveToFirst() == true) {
                val totalIndex = cursor.getColumnIndex("total")
                if (totalIndex != -1 && !cursor.isNull(totalIndex)) {
                    total = cursor.getDouble(totalIndex)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting total expenses: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return total
    }

    //Gets total expenses by category for a user in a specific time period
    fun getTotalExpensesByCategory(userId: Long, startDate: String, endDate: String): Map<Long, Double> {
        val categoryTotals = mutableMapOf<Long, Double>()
        val db = dbHelper.readableDatabase

        val selection = """
            ${BudgetDatabase.COLUMN_EXPENSE_USER_ID} = ? AND 
            ${BudgetDatabase.COLUMN_EXPENSE_DATE} >= ? AND
            ${BudgetDatabase.COLUMN_EXPENSE_DATE} <= ?
        """.trimIndent()

        val selectionArgs = arrayOf(userId.toString(), startDate, endDate)

        val query = """
            SELECT ${BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID}, 
                   SUM(${BudgetDatabase.COLUMN_EXPENSE_AMOUNT}) as category_total 
            FROM ${BudgetDatabase.TABLE_EXPENSES}
            WHERE ${selection}
            GROUP BY ${BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID}
        """.trimIndent()

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(query, selectionArgs)

            while (cursor?.moveToNext() == true) {
                val categoryIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID)
                val categoryTotalIndex = cursor.getColumnIndex("category_total")

                if (categoryIdIndex != -1 && categoryTotalIndex != -1) {
                    val categoryId = cursor.getLong(categoryIdIndex)
                    val categoryTotal = cursor.getDouble(categoryTotalIndex)
                    categoryTotals[categoryId] = categoryTotal
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting category totals: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return categoryTotals
    }

    //Searchs expenses by description for a user
    fun searchExpensesByDescription(userId: Long, searchQuery: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = dbHelper.readableDatabase

        val selection = """
            ${BudgetDatabase.COLUMN_EXPENSE_USER_ID} = ? AND 
            ${BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION} LIKE ?
        """.trimIndent()

        val selectionArgs = arrayOf(userId.toString(), "%$searchQuery%")

        var cursor: Cursor? = null

        try {
            cursor = db.query(
                BudgetDatabase.TABLE_EXPENSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "${BudgetDatabase.COLUMN_EXPENSE_DATE} DESC"
            )

            while (cursor?.moveToNext() == true) {
                val idIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_ID)
                val amountIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_AMOUNT)
                val dateIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_DATE)
                val descriptionIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION)
                val categoryIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID)
                val userIdIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_USER_ID)
                val receiptPathIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_EXPENSE_RECEIPT_PATH)

                val expense = Expense(
                    id = cursor.getLong(idIndex),
                    amount = cursor.getDouble(amountIndex),
                    date = cursor.getString(dateIndex),
                    description = cursor.getString(descriptionIndex),
                    categoryId = cursor.getLong(categoryIdIndex),
                    userId = cursor.getLong(userIdIndex),
                    receiptPath = if (receiptPathIndex != -1 && !cursor.isNull(receiptPathIndex))
                        cursor.getString(receiptPathIndex) else null
                )
                expenses.add(expense)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while searching expenses: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return expenses
    }

    //Batch inserts multiple expenses (for importing or syncing)
    fun batchInsert(expenses: List<Expense>): Int {
        val db = dbHelper.writableDatabase
        var successCount = 0

        try {
            db.beginTransaction()

            for (expense in expenses) {
                val values = ContentValues().apply {
                    put(BudgetDatabase.COLUMN_EXPENSE_AMOUNT, expense.amount)
                    put(BudgetDatabase.COLUMN_EXPENSE_DATE, expense.date)
                    put(BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION, expense.description)
                    put(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID, expense.categoryId)
                    put(BudgetDatabase.COLUMN_EXPENSE_USER_ID, expense.userId)
                    expense.receiptPath?.let { put(BudgetDatabase.COLUMN_EXPENSE_RECEIPT_PATH, it) }
                }

                val id = db.insert(BudgetDatabase.TABLE_EXPENSES, null, values)
                if (id != -1L) {
                    successCount++
                }
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "Exception during batch insert: ${e.message}")
        } finally {
            if (db.inTransaction()) {
                db.endTransaction()
            }
            db.close()
        }

        return successCount
    }

    //Deletes all expenses for a user
    fun deleteAllExpensesForUser(userId: Long): Int {
        val db = dbHelper.writableDatabase
        val selection = "${BudgetDatabase.COLUMN_EXPENSE_USER_ID} = ?"
        val selectionArgs = arrayOf(userId.toString())

        return try {
            val rowsAffected = db.delete(BudgetDatabase.TABLE_EXPENSES, selection, selectionArgs)
            rowsAffected
        } catch (e: Exception) {
            Log.e(TAG, "Exception while deleting all expenses: ${e.message}")
            0
        } finally {
            db.close()
        }
    }
}