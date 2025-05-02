package vcmsa.projects.personalbudgettingcorp

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.*
import java.util.Locale


class AddEditExpenseActivity : AppCompatActivity() {

    private lateinit var expenseDao: ExpenseDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var amountEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button
    private var expenseId: Long = -1
    private var userId: Long = 1
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var categories: List<com.personalbudgettingcorp.data.models.Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_expense)

        // Initialize UI elements
        amountEditText = findViewById(R.id.amountEditText)
        dateEditText = findViewById(R.id.dateEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        saveButton = findViewById(R.id.saveButton)

        // Initialize DAOs
        expenseDao = ExpenseDao(this)
        categoryDao = CategoryDao(this)

        // Get expense ID from intent (if editing)
        expenseId = intent.getLongExtra("expenseId", -1)

        // Load categories and populate the spinner
        loadCategories()

        if (expenseId != -1L) {
            // Load expense data for editing
            loadExpenseData()
            saveButton.text = "Update Expense"
        } else {
            dateEditText.setText(dateFormat.format(Date())) // Set default date for new expense
        }

        // Set click listener for the save button
        saveButton.setOnClickListener {
            saveExpense()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        expenseDao.close()
        categoryDao.close()
    }

    private fun loadCategories() {
        categories = categoryDao.getCategoriesForUser(userId) // Load categories for the user
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun loadExpenseData() {
        val expense = expenseDao.getExpenseById(expenseId)
        expense?.let {
            amountEditText.setText(it.amount.toString())
            dateEditText.setText(dateFormat.format(it.date))
            descriptionEditText.setText(it.description)
            // Set the spinner selection based on the categoryId
            val categoryIndex = categories.indexOfFirst { category -> category.id == it.categoryId }
            if (categoryIndex != -1) {
                categorySpinner.setSelection(categoryIndex)
            }
        }
    }

    private fun saveExpense() {
        val amountStr = amountEditText.text.toString().trim()
        val dateStr = dateEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val selectedCategoryName = categorySpinner.selectedItem as String
        val selectedCategory = categories.find { it.name == selectedCategoryName }

        if (amountStr.isEmpty() || dateStr.isEmpty() || description.isEmpty() || selectedCategory == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show()
            return
        }

        val date = try {
            dateFormat.parse(dateStr)
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            return
        }

        if (expenseId == -1L) {
            // Add new expense
            val newExpense = Expense(
                amount = amount,
                date = date,
                description = description,
                categoryId = selectedCategory.id,
                userId = userId
            )
            val result = expenseDao.insert(newExpense)
            if (result != -1L) {
                Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Update existing expense
            val updatedExpense = Expense(
                id = expenseId,
                amount = amount,
                date = date,
                description = description,
                categoryId = selectedCategory.id,
                userId = userId
            )
            val rowsAffected = expenseDao.update(updatedExpense)
            if (rowsAffected > 0) {
                Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update expense", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
