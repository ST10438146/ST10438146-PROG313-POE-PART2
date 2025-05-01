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
}