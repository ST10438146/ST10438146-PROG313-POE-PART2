package vcmsa.projects.personalbudgettingcorp

import android.util.Log

//Repository class for Category-related operations
class CategoryRepository(private val categoryDao: CategoryDao) {
    private val TAG = "CategoryRepository"

    //Gets all categories for a user
    fun getCategoriesForUser(userId: Long): List<Category> {
        return try {
            categoryDao.getCategoriesForUser(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting categories: ${e.message}")
            emptyList()
        }
    }

    //Creates a new category
    fun createCategory(category: Category): Category? {
        try {
            val categoryId = categoryDao.insert(category)
            if (categoryId == -1L) {
                Log.e(TAG, "Failed to create category: ${category.name}")
                return null
            }
            return category.copy(id = categoryId)
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating category: ${e.message}")
            return null
        }
    }

    //Updates a category
    fun updateCategory(category: Category): Boolean {
        return try {
            val rowsAffected = categoryDao.update(category)
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating category: ${e.message}")
            false
        }
    }

    //Deletes a category
    fun deleteCategory(categoryId: Long): Boolean {
        return try {
            val rowsAffected = categoryDao.delete(categoryId)
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Exception deleting category: ${e.message}")
            false
        }
    }
}