package vcmsa.projects.personalbudgettingcorp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class BudgetDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "BudgetDatabase"
        private const val DATABASE_NAME = "budget_tracker.db"
        private const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_USERS = "users"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_EXPENSES = "expenses"
        const val TABLE_BUDGETS = "budgets" // New table for budget goals

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

        // Budgets Table Columns
        const val KEY_MONTH = "month" // e.g., "2025-05"
        const val KEY_TOTAL_BUDGET = "total_budget"
        const val KEY_CATEGORY_LIMIT = "category_limit"

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