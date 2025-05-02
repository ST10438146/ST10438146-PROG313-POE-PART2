package vcmsa.projects.personalbudgettingcorp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class BudgetDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        const val COLUMN_CATEGORY_NAME = "name"
        const val COLUMN_CATEGORY_ID = "id"
        const val COLUMN_CATEGORY_USER_ID = "userid"
        const val COLUMN_EXPENSE_USER_ID = "userid"
        const val COLUMN_EXPENSE_CATEGORY_ID = "categoryld"
        const val COLUMN_EXPENSE_DESCRIPTION = "description"
        const val COLUMN_EXPENSE_DATE = "date"
        const val COLUMN_EXPENSE_AMOUNT = "amount"
        private const val TAG = "BudgetDatabase"
        private const val DATABASE_NAME = "budget_tracker.db"
        private const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_USERS = "users"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_EXPENSES = "expenses"
        const val TABLE_BUDGETS = "budgets"

        // Column Names
        const val KEY_ID = "id"

        // Users Table Columns
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"

        // Categories Table Columns
        const val KEY_CATEGORY_NAME = "category_name"

        // Expenses Table Columns
        const val KEY_AMOUNT = "amount"
        const val KEY_DATE = "date"
        const val KEY_DESCRIPTION = "description"
        const val KEY_CATEGORY_ID = "category_id"
        const val KEY_PHOTO = "photo"

        // Achievement table for gamification
        const val TABLE_ACHIEVEMENTS = "achievements"
        const val COLUMN_ACHIEVEMENT_ID = "id"
        const val COLUMN_ACHIEVEMENT_NAME = "name"
        const val COLUMN_ACHIEVEMENT_DESCRIPTION = "description"
        const val COLUMN_ACHIEVEMENT_UNLOCKED = "unlocked"
        const val COLUMN_ACHIEVEMENT_UNLOCK_DATE = "unlock_date"
        const val COLUMN_ACHIEVEMENT_USER_ID = "user_id"

        // Budgets Table Columns
        const val KEY_MONTH = "month" // e.g., "2025-05"
        const val KEY_TOTAL_BUDGET = "total_budget"
        const val KEY_CATEGORY_LIMIT = "category_limit"
        const val COLUMN_BUDGET_CATEGORY_LIMITS = "category_limits"

        // SQL to create tables
        private const val CREATE_USERS_TABLE =
            "CREATE TABLE $TABLE_USERS ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$KEY_USERNAME TEXT UNIQUE NOT NULL, " +
                    "$KEY_PASSWORD TEXT NOT NULL)"

        private const val CREATE_CATEGORIES_TABLE =
            "CREATE TABLE $TABLE_CATEGORIES ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$KEY_CATEGORY_NAME TEXT UNIQUE NOT NULL)"

        private const val CREATE_EXPENSES_TABLE =
            "CREATE TABLE $TABLE_EXPENSES ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$KEY_AMOUNT REAL NOT NULL, " +
                    "$KEY_DATE TEXT NOT NULL, " +
                    "$KEY_DESCRIPTION TEXT, " +
                    "$KEY_CATEGORY_ID INTEGER NOT NULL, " +
                    "$KEY_PHOTO BLOB, " +
                    "FOREIGN KEY($KEY_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($KEY_ID))"

        private const val CREATE_BUDGETS_TABLE =
            "CREATE TABLE $TABLE_BUDGETS ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$KEY_MONTH TEXT NOT NULL, " +
                    "$KEY_TOTAL_BUDGET REAL, " +
                    "$KEY_CATEGORY_ID INTEGER, " +
                    "$KEY_CATEGORY_LIMIT REAL, " +
                    "FOREIGN KEY($KEY_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($KEY_ID), " +
                    "UNIQUE ($KEY_MONTH, $KEY_CATEGORY_ID) ON CONFLICT REPLACE)" // Ensure one limit per category per month
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createAchievementsTable = """
        CREATE TABLE $TABLE_ACHIEVEMENTS (
            $COLUMN_ACHIEVEMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_ACHIEVEMENT_NAME TEXT NOT NULL,
            $COLUMN_ACHIEVEMENT_DESCRIPTION TEXT NOT NULL,
            $COLUMN_ACHIEVEMENT_UNLOCKED INTEGER DEFAULT 0,
            $COLUMN_ACHIEVEMENT_UNLOCK_DATE TEXT,
            $COLUMN_ACHIEVEMENT_USER_ID INTEGER,
            FOREIGN KEY ($COLUMN_ACHIEVEMENT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ACHIEVEMENT_USER_ID)
        )
    """.trimIndent()
        db.execSQL(createAchievementsTable)
        insertDefaultAchievements(db) // Inserts default achievements
        try {
            db?.execSQL(CREATE_USERS_TABLE)
            db?.execSQL(CREATE_CATEGORIES_TABLE)
            db?.execSQL(CREATE_EXPENSES_TABLE)
            db?.execSQL(CREATE_BUDGETS_TABLE)
            Log.i(TAG, "Database tables created successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating database tables: ${e.message}")
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
            arrayOf("Data Analyst", "View spending reports for 5 consecutive months"),
            arrayOf("First Step", "Log your first expense"),
            arrayOf("Daily Tracker", "Open the app and log an expense 7 days in a row"),
            arrayOf("100 Club", "Log 100 expenses")
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


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handles database schema upgrades if needed
        Log.w(TAG, "Upgrading database from version $oldVersion to $newVersion")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGETS")
        onCreate(db)
    }
}