package vcmsa.projects.personalbudgettingcorp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class BudgetDatabase (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "BudgetDatabase"
        private const val DATABASE_NAME = "budget_tracker.db"
        private const val DATABASE_VERSION = 1

        // User table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_USERNAME = "username"
        const val COLUMN_USER_PASSWORD = "password"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_ID = "id"

        // Category table
        const val TABLE_CATEGORIES = "categories"
        const val COLUMN_CATEGORY_ID = "id"
        const val COLUMN_CATEGORY_NAME = "name"
        const val COLUMN_CATEGORY_COLOR = "color"
        const val COLUMN_CATEGORY_USER_ID = "user_id"
        const val COLUMN_CATEGORY_BUDGET = "budget_limit"


        // Expense table
        const val TABLE_EXPENSES = "expenses"
        const val COLUMN_EXPENSE_ID = "id"
        const val COLUMN_EXPENSE_AMOUNT = "amount"
        const val COLUMN_EXPENSE_DATE = "date"
        const val COLUMN_EXPENSE_DESCRIPTION = "description"
        const val COLUMN_EXPENSE_CATEGORY_ID = "category_id"
        const val COLUMN_EXPENSE_USER_ID = "user_id"
        const val COLUMN_EXPENSE_RECEIPT_PATH = "receipt_path"

        // Budget goal table
        const val TABLE_BUDGET_GOALS = "budget_goals"
        const val COLUMN_BUDGET_ID = "id"
        const val COLUMN_BUDGET_AMOUNT = "amount"
        const val COLUMN_BUDGET_MONTH = "month"
        const val COLUMN_BUDGET_YEAR = "year"
        const val COLUMN_BUDGET_USER_ID = "user_id"

        // Achievement table for gamification
        const val TABLE_ACHIEVEMENTS = "achievements"
        const val COLUMN_ACHIEVEMENT_ID = "id"
        const val COLUMN_ACHIEVEMENT_NAME = "name"
        const val COLUMN_ACHIEVEMENT_DESCRIPTION = "description"
        const val COLUMN_ACHIEVEMENT_UNLOCKED = "unlocked"
        const val COLUMN_ACHIEVEMENT_UNLOCK_DATE = "unlock_date"
        const val COLUMN_ACHIEVEMENT_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Creates users table
            val createUsersTable = """
                CREATE TABLE $TABLE_USERS (
                    $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_USERNAME TEXT UNIQUE NOT NULL,
                    $COLUMN_USER_PASSWORD TEXT NOT NULL
                )
            """.trimIndent()

            // Register a user
            fun registerUser(username: String, password: String): Boolean {
                return try {
                    val db = writableDatabase
                    val values = ContentValues().apply {
                        put("username", username)
                        put("password", password)
                    }
                    db.insertOrThrow("Users", null, values)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            // Add a new user
            fun addUser(username: String, password: String): Long {
                val db = this.writableDatabase
                val values = ContentValues()
                values.put(KEY_USERNAME, username)
                values.put(KEY_PASSWORD, password)

                // Insert Row
                val id = db.insert(TABLE_USERS, null, values)
                db.close()
                return id
            }

            // Get a user by username
            fun getUserByUsername(username: String): android.database.Cursor? {
                val db = this.readableDatabase
                val cursor = db.query(TABLE_USERS, arrayOf(KEY_ID, KEY_USERNAME, KEY_PASSWORD), "$KEY_USERNAME=?", arrayOf(username), null, null, null)
                return cursor
            }

            // Login a user
            fun loginUser(username: String, password: String): Boolean {
                val db = readableDatabase
                val cursor = db.rawQuery(
                    "SELECT * FROM Users WHERE username = ? AND password = ?",
                    arrayOf(username, password)
                )
                val success = cursor.count > 0
                cursor.close()
                return success
            }
            // Creates categories table
            val createCategoriesTable = """
                CREATE TABLE $TABLE_CATEGORIES (
                    $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_CATEGORY_NAME TEXT NOT NULL,
                    $COLUMN_CATEGORY_COLOR TEXT NOT NULL,
                    $COLUMN_CATEGORY_BUDGET REAL DEFAULT 0.0,
                    $COLUMN_CATEGORY_USER_ID INTEGER,
                    FOREIGN KEY ($COLUMN_CATEGORY_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
                )
            """.trimIndent()

            // Creates expenses table
            val createExpensesTable = """
                CREATE TABLE $TABLE_EXPENSES (
                    $COLUMN_EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_EXPENSE_AMOUNT REAL NOT NULL,
                    $COLUMN_EXPENSE_DATE TEXT NOT NULL,
                    $COLUMN_EXPENSE_DESCRIPTION TEXT,
                    $COLUMN_EXPENSE_RECEIPT_PATH TEXT,
                    $COLUMN_EXPENSE_CATEGORY_ID INTEGER,
                    $COLUMN_EXPENSE_USER_ID INTEGER,
                    FOREIGN KEY ($COLUMN_EXPENSE_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_CATEGORY_ID),
                    FOREIGN KEY ($COLUMN_EXPENSE_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
                )
            """.trimIndent()

            // Creates budget goals table
            val createBudgetGoalsTable = """
                CREATE TABLE $TABLE_BUDGET_GOALS (
                    $COLUMN_BUDGET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_BUDGET_AMOUNT REAL NOT NULL,
                    $COLUMN_BUDGET_MONTH INTEGER NOT NULL,
                    $COLUMN_BUDGET_YEAR INTEGER NOT NULL,
                    $COLUMN_BUDGET_USER_ID INTEGER,
                    FOREIGN KEY ($COLUMN_BUDGET_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
                )
            """.trimIndent()

            // Creates achievements table for gamification
            val createAchievementsTable = """
                CREATE TABLE $TABLE_ACHIEVEMENTS (
                    $COLUMN_ACHIEVEMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_ACHIEVEMENT_NAME TEXT NOT NULL,
                    $COLUMN_ACHIEVEMENT_DESCRIPTION TEXT NOT NULL,
                    $COLUMN_ACHIEVEMENT_UNLOCKED INTEGER DEFAULT 0,
                    $COLUMN_ACHIEVEMENT_UNLOCK_DATE TEXT,
                    $COLUMN_ACHIEVEMENT_USER_ID INTEGER,
                    FOREIGN KEY ($COLUMN_ACHIEVEMENT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
                )
            """.trimIndent()

            // Executes the SQL statements
            db.execSQL(createUsersTable)
            db.execSQL(createCategoriesTable)
            db.execSQL(createExpensesTable)
            db.execSQL(createBudgetGoalsTable)
            db.execSQL(createAchievementsTable)

            // Inserts default categories
            insertDefaultCategories(db)

            // Inserts default achievements
            insertDefaultAchievements(db)

            Log.i(TAG, "Database tables created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating database: ${e.message}")
            throw e
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handles database version upgrades
        if (oldVersion < newVersion) {
            // For simplicity, drops and recreate tables
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ACHIEVEMENTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET_GOALS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            onCreate(db)
        }
    }

    private fun insertDefaultCategories(db: SQLiteDatabase) {
        // Inserts default categories that will be available to all users
        val categories = arrayOf(
            arrayOf("Groceries", "#4CAF50"),
            arrayOf("Food & Dining", "#FF5722"),
            arrayOf("Transport", "#2196F3"),
            arrayOf("Rent", "#9C27B0"),
            arrayOf("Medicine", "#F44336"),
            arrayOf("Entertainment", "#FFC107")
        )

        for (categoryData in categories) {
            val insertQuery = """
                INSERT INTO $TABLE_CATEGORIES ($COLUMN_CATEGORY_NAME, $COLUMN_CATEGORY_COLOR)
                VALUES ('${categoryData[0]}', '${categoryData[1]}')
            """.trimIndent()

            try {
                db.execSQL(insertQuery)
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting default category ${categoryData[0]}: ${e.message}")
            }
        }
    }

    private fun insertDefaultAchievements(db: SQLiteDatabase) {
        // Inserts default achievements for gamification
        val achievements = arrayOf(
            arrayOf("Budget Master", "Stay within your total budget for 3 consecutive months"),
            arrayOf("Expense Tracker", "Log expenses for 7 consecutive days"),
            arrayOf("Category Champion", "Create 5 custom spending categories"),
            arrayOf("Receipt Collector", "Attach receipts to 20 expenses"),
            arrayOf("Savings Star", "Save 20% of your monthly budget"),
            arrayOf("Data Analyst", "View spending reports for 5 consecutive months")
        )

        for (achievementData in achievements) {
            val insertQuery = """
                INSERT INTO $TABLE_ACHIEVEMENTS ($COLUMN_ACHIEVEMENT_NAME, $COLUMN_ACHIEVEMENT_DESCRIPTION)
                VALUES ('${achievementData[0]}', '${achievementData[1]}')
            """.trimIndent()

            try {
                db.execSQL(insertQuery)
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting default achievement ${achievementData[0]}: ${e.message}")
            }
        }

    }
}