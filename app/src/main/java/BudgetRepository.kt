package vcmsa.projects.personalbudgettingcorp

import android.util.Log
import vcmsa.projects.personalbudgettingcorp.vcmsa.projects.personalbudgettingcorp.BudgetGoalDao
import java.util.*

//Repository class for Budget-related operations
class BudgetRepository(private val budgetGoalDao: BudgetGoalDao) {
    private val TAG = "BudgetRepository"

    //Sets a monthly budget goal
    fun setMonthlyBudgetGoal(userId: Long, amount: Double, month: Int, year: Int): Boolean {
        try {
            val budgetGoal = BudgetGoal(
                amount = amount,
                month = month,
                year = year,
                userId = userId
            )

            val rowsAffected = budgetGoalDao.update(budgetGoal)
            return rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Exception setting budget goal: ${e.message}")
            return false
        }
    }

    //Gets a budget goal for a specific month
    fun getBudgetGoal(userId: Long, month: Int, year: Int): BudgetGoal? {
        return try {
            budgetGoalDao.getBudgetGoal(userId, month, year)
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting budget goal: ${e.message}")
            null
        }
    }

    //Gets the current month's budget goal
    fun getCurrentMonthBudgetGoal(userId: Long): BudgetGoal? {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-based
        val currentYear = calendar.get(Calendar.YEAR)

        return getBudgetGoal(userId, currentMonth, currentYear)
    }
}
