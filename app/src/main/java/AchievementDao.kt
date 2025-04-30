package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

//Data Access Object for Achievement-related database operations

class AchievementDao(private val dbHelper: BudgetDatabase) {
    private val TAG = "AchievementDao"

    //Assigns default achievements to a user
    fun assignDefaultAchievementsToUser(userId: Long): Int {
        val db = dbHelper.readableDatabase

        // Gets all default achievements
        val query = "SELECT * FROM ${BudgetDatabase.TABLE_ACHIEVEMENTS} WHERE ${BudgetDatabase.COLUMN_ACHIEVEMENT_USER_ID} IS NULL"

        var cursor: Cursor? = null
        var assignedCount = 0

        try {
            cursor = db.rawQuery(query, null)

            while (cursor?.moveToNext() == true) {
                val idIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_ACHIEVEMENT_ID)
                val nameIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_ACHIEVEMENT_NAME)
                val descIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_ACHIEVEMENT_DESCRIPTION)

                // Checks if columns exist
                if (idIndex != -1 && nameIndex != -1 && descIndex != -1) {
                    val name = cursor.getString(nameIndex)
                    val description = cursor.getString(descIndex)

                    // Creates a new achievement for this user
                    val achievement = Achievement(
                        name = name,
                        description = description,
                        userId = userId
                    )

                    if (insert(achievement) != -1L) {
                        assignedCount++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while assigning achievements: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return assignedCount
    }
    //Inserts a new achievement
    fun insert(achievement: Achievement): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_ACHIEVEMENT_NAME, achievement.name)
            put(BudgetDatabase.COLUMN_ACHIEVEMENT_DESCRIPTION, achievement.description)
            put(BudgetDatabase.COLUMN_ACHIEVEMENT_UNLOCKED, if (achievement.unlocked) 1 else 0)
            achievement.unlockDate?.let { put(BudgetDatabase.COLUMN_ACHIEVEMENT_UNLOCK_DATE, it) }
            achievement.userId?.let { put(BudgetDatabase.COLUMN_ACHIEVEMENT_USER_ID, it) }
        }

        return try {
            val id = db.insert(BudgetDatabase.TABLE_ACHIEVEMENTS, null, values)
            if (id == -1L) {
                Log.e(TAG, "Failed to insert achievement: ${achievement.name}")
            }
            id
        } catch (e: Exception) {
            Log.e(TAG, "Exception while inserting achievement: ${e.message}")
            -1
        } finally {
            db.close()
        }
    }

    //Updates an achievement's unlock status
    fun updateUnlockStatus(achievementId: Long, unlocked: Boolean, unlockDate: String?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(BudgetDatabase.COLUMN_ACHIEVEMENT_UNLOCKED, if (unlocked) 1 else 0)
            if (unlocked && unlockDate != null) {
                put(BudgetDatabase.COLUMN_ACHIEVEMENT_UNLOCK_DATE, unlockDate)
            } else {
                putNull(BudgetDatabase.COLUMN_ACHIEVEMENT_UNLOCK_DATE)
            }
        }

        val selection = "${BudgetDatabase.COLUMN_ACHIEVEMENT_ID} = ?"
        val selectionArgs = arrayOf(achievementId.toString())

        return try {
            val rowsAffected = db.update(
                BudgetDatabase.TABLE_ACHIEVEMENTS,
                values,
                selection,
                selectionArgs
            )
            rowsAffected
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating achievement status: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    //Gets all achievements for a user
    fun getAchievementsForUser(userId: Long): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        val db = dbHelper.readableDatabase

        val selection = "${BudgetDatabase.COLUMN_ACHIEVEMENT_USER_ID} = ?"
        val selectionArgs = arrayOf(userId.toString())

        var cursor: Cursor? = null

        try {
            cursor = db.query(
                BudgetDatabase.TABLE_ACHIEVEMENTS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "${BudgetDatabase.COLUMN_ACHIEVEMENT_UNLOCKED} DESC, ${BudgetDatabase.COLUMN_ACHIEVEMENT_NAME} ASC"
            )

            while (cursor?.moveToNext() == true) {
                val idIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_ACHIEVEMENT_ID)
                val nameIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_ACHIEVEMENT_NAME)
                val descIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_ACHIEVEMENT_DESCRIPTION)
                val unlockedIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_ACHIEVEMENT_UNLOCKED)
                val dateIndex = cursor.getColumnIndex(BudgetDatabase.COLUMN_ACHIEVEMENT_UNLOCK_DATE)

                // Check if columns exist
                if (idIndex != -1 && nameIndex != -1 && descIndex != -1 && unlockedIndex != -1) {
                    val id = cursor.getLong(idIndex)
                    val name = cursor.getString(nameIndex)
                    val description = cursor.getString(descIndex)
                    val unlocked = cursor.getInt(unlockedIndex) == 1
                    val unlockDate = if (dateIndex != -1 && !cursor.isNull(dateIndex)) {
                        cursor.getString(dateIndex)
                    } else {
                        null
                    }

                    achievements.add(Achievement(id, name, description, unlocked, unlockDate, userId))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting achievements: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }

        return achievements
    }
}