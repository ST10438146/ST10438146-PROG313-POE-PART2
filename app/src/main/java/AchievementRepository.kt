package vcmsa.projects.personalbudgettingcorp

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

//Repository class for Achievement-related operations
class AchievementRepository(private val achievementDao: AchievementDao) {
    private val TAG = "AchievementRepository"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    //Sets up achievements for a new user
    fun setupAchievementsForNewUser(userId: Long): Int {
        return try {
            achievementDao.assignDefaultAchievementsToUser(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Exception setting up achievements: ${e.message}")
            0
        }
    }

    //Gets all achievements for a user
    fun getAchievementsForUser(userId: Long): List<Achievement> {
        return try {
            achievementDao.getAchievementsForUser(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting achievements: ${e.message}")
            emptyList()
        }
    }

    //Unlocks an achievement
    fun unlockAchievement(achievementId: Long): Boolean {
        val currentDate = dateFormat.format(Date())
        return try {
            val rowsAffected = achievementDao.updateUnlockStatus(achievementId, true, currentDate)
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Exception unlocking achievement: ${e.message}")
            false
        }
    }

    //Checks for achievements to unlock based on user activity
    fun checkAndUnlockAchievements(
        userId: Long,
        expenseRepository: ExpenseRepository,
        categoryRepository: CategoryRepository,
        budgetRepository: BudgetRepository
    ) {
        try {
            val achievements = getAchievementsForUser(userId)
            val currentDate = Calendar.getInstance()

            // Checks for expense logging streak
            val expenses = expenseRepository.getExpensesForUserInDateRange(
                userId,
                Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000), // 7 days ago
                Date()
            )

            // Groups expenses by day
            val expensesByDay = expenses.groupBy { it.date }

            // Checks if user has logged expenses for 7 consecutive days
            val today = dateFormat.format(Date())
            val calendar = Calendar.getInstance()
            var consecutiveDays = 0

            for (i in 0 until 7) {
                calendar.add(Calendar.DAY_OF_MONTH, -i)
                val date = dateFormat.format(calendar.time)

                if (expensesByDay.containsKey(date)) {
                    consecutiveDays++
                } else {
                    break
                }

                // Resets calendar for next iteration
                calendar.time = Date()
            }

            // Unlocks "Expense Tracker" achievement if 7 consecutive days
            if (consecutiveDays >= 7) {
                val expenseTrackerAchievement = achievements.find {
                    it.name == "Expense Tracker" && !it.unlocked
                }

                expenseTrackerAchievement?.let {
                    unlockAchievement(it.id)
                }
            }

            // Checks for category creation (Category Champion)
            val categories = categoryRepository.getCategoriesForUser(userId)
            val userCreatedCategories = categories.filter { it.userId == userId }

            if (userCreatedCategories.size >= 5) {
                val categoryChampionAchievement = achievements.find {
                    it.name == "Category Champion" && !it.unlocked
                }

                categoryChampionAchievement?.let {
                    unlockAchievement(it.id)
                }
            }

            // Checks for receipt collection (Receipt Collector)
            val expensesWithReceipts = expenses.count { !it.receiptPath.isNullOrEmpty() }

            if (expensesWithReceipts >= 20) {
                val receiptCollectorAchievement = achievements.find {
                    it.name == "Receipt Collector" && !it.unlocked
                }

                receiptCollectorAchievement?.let {
                    unlockAchievement(it.id)
                }
            }


        } catch (e: Exception) {
            Log.e(TAG, "Exception checking achievements: ${e.message}")
        }
    }
}