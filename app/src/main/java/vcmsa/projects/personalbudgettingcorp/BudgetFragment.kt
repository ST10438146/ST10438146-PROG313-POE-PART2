package vcmsa.projects.personalbudgettingcorp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import vcmsa.projects.personalbudgettingcorp.vcmsa.projects.personalbudgettingcorp.BudgetDao
import java.util.HashMap

class BudgetFragment : Fragment() {

    private lateinit var budgetDao: BudgetDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var setBudgetButton: Button
    private lateinit var viewBudgetButton: Button
    private var userId: Long = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        // Initializes UI elements
        setBudgetButton = view.findViewById(R.id.setBudgetButton)
        viewBudgetButton = view.findViewById(R.id.viewBudgetButton)

        // Initializes DAOs
        budgetDao = BudgetDao(requireContext())
        categoryDao = CategoryDao(requireContext())

        // Sets click listeners
        setBudgetButton.setOnClickListener {
            showSetBudgetDialog()
        }

        viewBudgetButton.setOnClickListener {
            viewBudget()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        budgetDao.close()
        categoryDao.close()
    }

    private fun showSetBudgetDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_set_budget, null)
        dialogBuilder.setView(dialogView)

        val totalBudgetEditText = dialogView.findViewById<EditText>(R.id.totalBudgetEditText)
        val categoryLimitsLayout = dialogView.findViewById<LinearLayout>(R.id.categoryLimitsLayout)

        // Gets categories for the user
        val categories = categoryDao.getCategoriesForUser(userId)

        // Dynamically adds EditText fields for each category
        val categoryEditTexts = HashMap<Long, EditText>()
        for (category in categories) {
            val categoryTextView = android.widget.TextView(requireContext())
            categoryTextView.text = category.name
            categoryTextView.textSize = 18f
            categoryTextView.setPadding(0, 8, 0, 0)
            categoryLimitsLayout.addView(categoryTextView)

            val categoryLimitEditText = EditText(requireContext())
            categoryLimitEditText.hint = "Limit for ${category.name}"
            categoryLimitEditText.inputType = android.text.InputType.TYPE_NUMBER_DECIMAL
            categoryLimitsLayout.addView(categoryLimitEditText)
            categoryEditTexts[category.id] = categoryLimitEditText
        }

        dialogBuilder.setTitle("Set Budget")
        dialogBuilder.setPositiveButton("Save") { _, _ ->
            val totalBudgetStr = totalBudgetEditText.text.toString().trim()
            if (totalBudgetStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter total budget", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val totalBudget = totalBudgetStr.toDouble()

            val categoryLimits = HashMap<Long, Double>()
            var hasInvalidCategoryLimit = false
            for ((categoryId, editText) in categoryEditTexts) {
                val limitStr = editText.text.toString().trim()
                if (limitStr.isNotEmpty()) {
                    val limit = limitStr.toDouble()
                    categoryLimits[categoryId] = limit
                } else {
                    categoryLimits[categoryId] = 0.0  //set default value
                }
            }

            if (hasInvalidCategoryLimit) {
                Toast.makeText(requireContext(), "Please enter valid category limits", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Checks if a budget already exists for the user
            val existingBudget = budgetDao.getBudgetForUser(userId)
            if (existingBudget != null) {
                // Updates existing budget
                val updatedBudget = Budget(
                    id = existingBudget.id, // Use the existing budget ID
                    totalBudget = totalBudget,
                    userId = userId,
                    categoryLimits = categoryLimits
                )
                val rowsAffected = budgetDao.update(updatedBudget)
                if (rowsAffected > 0) {
                    Toast.makeText(requireContext(), "Budget updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update budget", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Insert new budget
                val newBudget = Budget(
                    totalBudget = totalBudget,
                    userId = userId,
                    categoryLimits = categoryLimits
                )
                val result = budgetDao.insert(newBudget)
                if (result != -1L) {
                    Toast.makeText(requireContext(), "Budget set successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to set budget", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun viewBudget() {
        val budget = budgetDao.getBudgetForUser(userId)
        if (budget == null) {
            Toast.makeText(requireContext(), "No budget set yet", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Your Budget")
        val message = StringBuilder().apply {
            append("Total Budget: $${budget.totalBudget}\n\n")
            append("Category Limits:\n")
            budget.categoryLimits.forEach { (categoryId, limit) ->
                val category = categoryDao.getCategoryById(categoryId) // Get category name
                append("  ${category?.name ?: "Unknown"}: $${limit}\n")
            }
        }.toString()
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}
