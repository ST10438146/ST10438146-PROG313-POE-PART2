package vcmsa.projects.personalbudgettingcorp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.ArrayList

class DashboardFragment : Fragment() {

    private lateinit var expenseDao: ExpenseDao
    private lateinit var barChart: BarChart
    private var userId: Long = 1 // Replace with actual user ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize UI elements
        barChart = view.findViewById(R.id.barChart)

        // Initialize ExpenseDao
        expenseDao = ExpenseDao(requireContext())

        // Load and display chart data
        loadBarChartData()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        expenseDao.close()
    }

    private fun loadBarChartData() {
        // Get expenses for the user
        val expenses = expenseDao.getExpensesForUser(userId)

        // Calculate total spending per category
        val categorySpending = mutableMapOf<String, Double>()
        expenses.forEach { expense ->
            val category = expenseDao.getCategoryById(expense.categoryId)?.name ?: "Unknown" // Get category name
            val amount = categorySpending[category] ?: 0.0
            categorySpending[category] = amount + expense.amount
        }

        // Prepare data for the chart
        val entries = ArrayList<BarEntry>()
        val categoryNames = ArrayList<String>()
        var xIndex = 0f
        for ((category, total) in categorySpending) {
            entries.add(BarEntry(xIndex, total.toFloat()))
            categoryNames.add(category)
            xIndex++
        }

        // Create BarDataSet and BarData
        val barDataSet = BarDataSet(entries, "Category Spending")
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        barDataSet.valueTextColor = android.graphics.Color.BLACK // Set label color
        val barData = BarData(barDataSet)
        barData.setValueTextSize(12f);

        // Configure the chart
        barChart.data = barData
        barChart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(categoryNames) // Set category names on X-axis
        barChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM // X-Axis at the bottom
        barChart.xAxis.textColor = android.graphics.Color.BLACK
        barChart.axisLeft.textColor = android.graphics.Color.BLACK
        barChart.description.isEnabled = false // Removes description label
        barChart.animateY(1000) // Animates the chart
        barChart.invalidate() // Refreshs the chart
    }
}