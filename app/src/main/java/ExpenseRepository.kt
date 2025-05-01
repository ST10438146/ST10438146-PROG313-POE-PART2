package vcmsa.projects.personalbudgettingcorp

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.Log
import com.google.type.Date
import java.util.Locale

//Repository class for Expense-related operations
class ExpenseRepository(private val expenseDao: ExpenseDao) {
    private val TAG = "ExpenseRepository"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    //Adds a new expense
    fun addExpense(expense: Expense): Expense? {
        try {
            val expenseId = expenseDao.insert(expense)
            if (expenseId == -1L) {
                Log.e(TAG, "Failed to add expense: ${expense.description}")
                return null
            }
            return expense.copy(id = expenseId)
        } catch (e: Exception) {
            Log.e(TAG, "Exception adding expense: ${e.message}")
            return null
        }
    }

    //Updates an expense
    fun updateExpense(expense: Expense): Boolean {
        return try {
            val rowsAffected = expenseDao.update(expense)
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating expense: ${e.message}")
            false
        }
    }

    //Deletes an expense
    fun deleteExpense(expenseId: Long): Boolean {
        return try {
            val rowsAffected = expenseDao.delete(expenseId)
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Exception deleting expense: ${e.message}")
            false
        }
    }

    //Gets expenses for a user within a date range
    fun getExpensesForUserInDateRange(userId: Long, startDate: Date, endDate: Date): List<Expense> {
        return try {
            val startDateStr = dateFormat.format(startDate)
            val endDateStr = dateFormat.format(endDate)
            expenseDao.getExpensesForUserInDateRange(userId, startDateStr, endDateStr)
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting expenses: ${e.message}")
            emptyList()
        }
    }

    //Gets expenses for the current month
    fun getExpensesForCurrentMonth(userId: Long): List<Expense> {
        val calendar = Calendar.getInstance()

        // Sets to first day of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.time

        // Sets to last day of current month
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time

        return getExpensesForUserInDateRange(userId, startDate, endDate)
    }

    //Gets total expenses by category for a date range
    fun getTotalExpensesByCategory(userId: Long, startDate: Date, endDate: Date): Map<Long, Double> {
        return try {
            val startDateStr = dateFormat.format(startDate)
            val endDateStr = dateFormat.format(endDate)
            expenseDao.getTotalExpensesByCategory(userId, startDateStr, endDateStr)
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting category totals: ${e.message}")
            emptyMap()
        }
    }

    //Gets daily expense totals for a date range
    fun getDailyExpenseTotals(userId: Long, startDate: Date, endDate: Date): Map<String, Double> {
        return try {
            val startDateStr = dateFormat.format(startDate)
            val endDateStr = dateFormat.format(endDate)
            expenseDao.getDailyExpenseTotals(userId, startDateStr, endDateStr)
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting daily totals: ${e.message}")
            emptyMap()
        }
    }
}