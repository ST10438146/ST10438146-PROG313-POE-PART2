package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExpenseDao(private val dbHelper: BudgetDatabase) {

    private val database: SQLiteDatabase = dbHelper.writableDatabase
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun <T> executeWithCatch(operation: () -> T?, errorMessage: String): T? {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e("ExpenseDao", "$errorMessage: ${e.message}")
            null
        }
    }

    private fun dateToString(date: Date): String = dateFormatter.format(date)
    private fun stringToDate(dateString: String): Date? =
        try { dateFormatter.parse(dateString) } catch (e: Exception) { null }
    private fun bitmapToByteArray(bitmap: Bitmap?): ByteArray? =
        bitmap?.let { ByteArrayOutputStream().apply { it.compress(Bitmap.CompressFormat.PNG, 0, this) }.toByteArray() }
    private fun byteArrayToBitmap(byteArray: ByteArray?): Bitmap? =
        byteArray?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

    fun addExpense(amount: Double, date: Date, description: String?, categoryId: Long, photo: Bitmap?): Long {
        val values = ContentValues().apply {
            put(BudgetDatabase.KEY_AMOUNT, amount)
            put(BudgetDatabase.KEY_DATE, dateToString(date))
            put(BudgetDatabase.KEY_DESCRIPTION, description)
            put(BudgetDatabase.KEY_CATEGORY_ID, categoryId)
            put(BudgetDatabase.KEY_PHOTO, bitmapToByteArray(photo))
        }
        return executeWithCatch({
            database.insert(BudgetDatabase.TABLE_EXPENSES, null, values)
        }, "Error adding expense") ?: -1
    }

    fun getAllExpenses(startDate: Date? = null, endDate: Date? = null): Cursor? {
        val selection: String?
        val selectionArgs: Array<String>?

        if (startDate != null && endDate != null) {
            selection = "${BudgetDatabase.KEY_DATE} BETWEEN ? AND ?"
            selectionArgs = arrayOf(dateToString(startDate), dateToString(endDate))
        } else {
            selection = null
            selectionArgs = null
        }

        return executeWithCatch({
            database.query(
                BudgetDatabase.TABLE_EXPENSES,
                arrayOf(
                    BudgetDatabase.KEY_ID,
                    BudgetDatabase.KEY_AMOUNT,
                    BudgetDatabase.KEY_DATE,
                    BudgetDatabase.KEY_DESCRIPTION,
                    BudgetDatabase.KEY_CATEGORY_ID,
                    BudgetDatabase.KEY_PHOTO
                ),
                selection,
                selectionArgs,
                null,
                null,
                "${BudgetDatabase.KEY_DATE} DESC"
            )
        }, "Error getting all expenses")
    }

    fun getExpensesByCategory(categoryId: Long, startDate: Date? = null, endDate: Date? = null): Cursor? {
        val selection: String?
        val selectionArgs: Array<String>?

        if (startDate != null && endDate != null) {
            selection = "${BudgetDatabase.KEY_CATEGORY_ID} = ? AND ${BudgetDatabase.KEY_DATE} BETWEEN ? AND ?"
            selectionArgs = arrayOf(categoryId.toString(), dateToString(startDate), dateToString(endDate))
        } else {
            selection = "${BudgetDatabase.KEY_CATEGORY_ID} = ?"
            selectionArgs = arrayOf(categoryId.toString())
        }

        return executeWithCatch({
            database.query(
                BudgetDatabase.TABLE_EXPENSES,
                arrayOf(
                    BudgetDatabase.KEY_ID,
                    BudgetDatabase.KEY_AMOUNT,
                    BudgetDatabase.KEY_DATE,
                    BudgetDatabase.KEY_DESCRIPTION,
                    BudgetDatabase.KEY_CATEGORY_ID,
                    BudgetDatabase.KEY_PHOTO
                ),
                selection,
                selectionArgs,
                null,
                null,
                "${BudgetDatabase.KEY_DATE} DESC"
            )
        }, "Error getting expenses by category")
    }


    fun getExpenseById(expenseId: Long): Cursor? {
        return executeWithCatch({
            database.query(
                BudgetDatabase.TABLE_EXPENSES,
                arrayOf(
                    BudgetDatabase.KEY_ID,
                    BudgetDatabase.KEY_AMOUNT,
                    BudgetDatabase.KEY_DATE,
                    BudgetDatabase.KEY_DESCRIPTION,
                    BudgetDatabase.KEY_CATEGORY_ID,
                    BudgetDatabase.KEY_PHOTO
                ),
                "${BudgetDatabase.KEY_ID} = ?",
                arrayOf(expenseId.toString()),
                null,
                null,
                null
            )
        }, "Error getting expense by ID")
    }

    fun close() {
        database.close()
    }
    /**
     * Inserts a new expense into the database.
     *
     * @param expense The Expense object to insert.
     * @return The row ID of the newly inserted expense, or -1 if an error occurred.
     */
    fun insert(expense: Expense): Long {
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_EXPENSE_AMOUNT, expense.amount)
            put(BudgetDatabase.COLUMN_EXPENSE_DATE, SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(expense.date))
            put(BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION, expense.description)
            put(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID, expense.categoryld)
            put(BudgetDatabase.COLUMN_EXPENSE_USER_ID, expense.userld)
            put(BudgetDatabase.COLUMN_EXPENSE_IMAGE_PATH, expense.imagePath)
        }

        return executeWithCatch({
            database.insert(BudgetDatabase.TABLE_EXPENSES, null, values)
        }, "Failed to insert expense") ?: -1
    }

    // Updates an existing expense in the database.

    fun update(expense: Expense): Int {
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_EXPENSE_AMOUNT, expense.amount)
            put(BudgetDatabase.COLUMN_EXPENSE_DATE, SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(expense.date))
            put(BudgetDatabase.COLUMN_EXPENSE_DESCRIPTION, expense.description)
            put(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID, expense.categoryld)
            put(BudgetDatabase.COLUMN_EXPENSE_USER_ID, expense.userld)
            put(BudgetDatabase.COLUMN_EXPENSE_IMAGE_PATH, expense.imagePath)

        }

        val selection = "${BudgetDatabase.COLUMN_EXPENSE_USER_ID} = ?"
        val selectionArgs = arrayOf(expense.id.toString())

        return executeWithCatch({
            database.update(BudgetDatabase.TABLE_EXPENSES, values, selection, selectionArgs)
        }, "Failed to update expense") ?: 0
    }

    fun close() {
        database.close()
        dbHelper.close()
    }

    fun getTotalExpensesByCategory(
        userId: Long,
        startDateStr: String?,
        endDateStr: String?
    ): Map<Long, Double> {
        val totalExpenses = mutableMapOf<Long, Double>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selection = StringBuilder("${BudgetDatabase.COLUMN_EXPENSE_USER_ID} = ?")
        val selectionArgs = mutableListOf(userId.toString())


        if (startDateStr != null) {
            selection.append(" AND ${BudgetDatabase.COLUMN_EXPENSE_DATE} >= ?")
            selectionArgs.add(startDateStr)
        }
        if (endDateStr != null) {
            selection.append(" AND ${BudgetDatabase.COLUMN_EXPENSE_DATE} <= ?")
            selectionArgs.add(endDateStr)
        }
        val query = "SELECT ${BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID}, SUM(${BudgetDatabase.COLUMN_EXPENSE_AMOUNT}) " +
                "FROM ${BudgetDatabase.TABLE_EXPENSES} " +
                "WHERE $selection " +
                "GROUP BY ${BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID}"


        executeWithCatch({
            val cursor: Cursor? = database.rawQuery(query, selectionArgs.toTypedArray())
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val categoryId = it.getLong(it.getColumnIndexOrThrow(BudgetDatabase.COLUMN_EXPENSE_CATEGORY_ID))
                        val totalAmount = it.getDouble(it.getColumnIndexOrThrow(1)) // Index 1 because of SUM()
                        totalExpenses[categoryId] = totalAmount
                    } while (it.moveToNext())
                }
            }
        }, "Failed to get total expenses by category")


        return totalExpenses
    }



    fun close() {
        database.close()
        dbHelper.close()
    }
}