package vcmsa.projects.personalbudgettingcorp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class ExpenseFragment : Fragment() {

    private lateinit var expenseDao: ExpenseDao
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var addExpenseButton: FloatingActionButton
    private lateinit var expenseAdapter: ExpenseAdapter
    private var userId: Long = 1 // Replace with actual user ID from session
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expense, container, false)

        // Initializes UI elements
        expensesRecyclerView = view.findViewById(R.id.expensesRecyclerView)
        addExpenseButton = view.findViewById(R.id.addExpenseButton)

        // Initializes ExpenseDao
        expenseDao = ExpenseDao(requireContext())

        // Sets up RecyclerView
        expensesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        expenseAdapter = ExpenseAdapter(mutableListOf(), this::onEditExpense, this::onDeleteExpense)
        expensesRecyclerView.adapter = expenseAdapter

        // Loads and displays expenses
        loadExpenses()

        // Sets click listener for the add expense button
        addExpenseButton.setOnClickListener {
            val intent = Intent(requireContext(), AddEditExpenseActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    override fun onDestroy() {
        super.onDestroy()
        expenseDao.close()
    }

    private fun loadExpenses() {
        val expenses = expenseDao.getExpensesForUser(userId)
        expenseAdapter.setExpenses(expenses)
        expenseAdapter.notifyDataSetChanged()
    }

    private fun onEditExpense(expenseId: Long) {
        val intent = Intent(requireContext(), AddEditExpenseActivity::class.java)
        intent.putExtra("expenseId", expenseId)
        startActivity(intent)
    }

    private fun onDeleteExpense(expenseId: Long) {
        // Shows confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Yes") { _, _ ->
                val rowsDeleted = expenseDao.delete(expenseId)
                if (rowsDeleted > 0) {
                    loadExpenses() // Refreshs the list after deletion
                    android.widget.Toast.makeText(requireContext(), "Expense deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Failed to delete expense", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

private class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onEditExpense: (Long) -> Unit,
    private val onDeleteExpense: (Long) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountTextView: android.widget.TextView = itemView.findViewById(R.id.amountTextView)
        val dateTextView: android.widget.TextView = itemView.findViewById(R.id.dateTextView)
        val descriptionTextView: android.widget.TextView = itemView.findViewById(R.id.descriptionTextView)
        val categoryTextView: android.widget.TextView = itemView.findViewById(R.id.categoryTextView)
        val editButton: android.widget.ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: android.widget.ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        val categoryDao = ExpenseDao(holder.itemView.context)
        val category = categoryDao.getCategoryById(expense.categoryId)
        holder.amountTextView.text = "Amount: $${expense.amount}"
        holder.dateTextView.text = "Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(expense.date)}"
        holder.descriptionTextView.text = "Description: ${expense.description}"
        holder.categoryTextView.text = "Category: ${category?.name ?: "Unknown"}"

        holder.editButton.setOnClickListener {
            onEditExpense(expense.id)
        }
        holder.deleteButton.setOnClickListener {
            onDeleteExpense(expense.id)
        }
        categoryDao.close()
    }

    override fun getItemCount() = expenses.size

    fun setExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
