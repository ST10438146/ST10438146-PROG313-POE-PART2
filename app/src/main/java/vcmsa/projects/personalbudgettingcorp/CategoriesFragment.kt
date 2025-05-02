package vcmsa.projects.personalbudgettingcorp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CategoriesFragment : Fragment() {

    private lateinit var categoryDao: CategoryDao
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var addCategoryButton: FloatingActionButton
    private lateinit var categoryAdapter: CategoryAdapter
    private var userId: Long = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        // Initializes UI elements
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView)
        addCategoryButton = view.findViewById(R.id.addCategoryButton)

        // Initializes CategoryDao
        categoryDao = CategoryDao(requireContext())

        // Set up RecyclerView
        categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoryAdapter(mutableListOf(), this::onDeleteCategory)  // Pass the delete callback
        categoriesRecyclerView.adapter = categoryAdapter

        // Loads and displays categories
        loadCategories()

        // Sets click listener for the add category button
        addCategoryButton.setOnClickListener {
            showAddCategoryDialog()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        categoryDao.close()
    }

    private fun loadCategories() {
        val categories = categoryDao.getCategoriesForUser(userId)
        categoryAdapter.setCategories(categories)
        categoryAdapter.notifyDataSetChanged() //Added to refresh the list.
    }

    private fun showAddCategoryDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_category, null)
        dialogBuilder.setView(dialogView)

        val nameEditText = dialogView.findViewById<EditText>(R.id.categoryNameEditText)
        val colorEditText = dialogView.findViewById<EditText>(R.id.categoryColorEditText)

        dialogBuilder.setTitle("Add Category")
        dialogBuilder.setPositiveButton("Add") { _: DialogInterface, _: Int ->
            val name = nameEditText.text.toString().trim()
            val color = colorEditText.text.toString().trim()

            if (name.isEmpty() || color.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both name and color", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (!color.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")))
            {
                Toast.makeText(requireContext(), "Please enter a valid color format (e.g., #RRGGBB)", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val newCategory = Category(name = name, color = color, userId = userId)
            val result = categoryDao.insert(newCategory)
            if (result != -1L) {
                loadCategories() // Refresh the list after adding
                Toast.makeText(requireContext(), "Category added successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to add category", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun onDeleteCategory(categoryId: Long) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Category")
        builder.setMessage("Are you sure you want to delete this category?")
        builder.setPositiveButton("Yes") { _, _ ->
            val rowsDeleted = categoryDao.delete(categoryId)
            if (rowsDeleted > 0) {
                loadCategories() // Refresh the list after deletion
                Toast.makeText(requireContext(), "Category deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to delete category", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}

private class CategoryAdapter(
    private var categories: List<Category>,
    private val onDeleteCategory: (Long) -> Unit // Callback function
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: android.widget.TextView = itemView.findViewById(R.id.categoryNameTextView)
        val colorView: View = itemView.findViewById(R.id.categoryColorView)
        val deleteButton: android.widget.ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.nameTextView.text = category.name
        holder.colorView.setBackgroundColor(android.graphics.Color.parseColor(category.color))
        holder.deleteButton.setOnClickListener {
            onDeleteCategory(category.id) // Call the delete callback
        }
    }

    override fun getItemCount() = categories.size

    fun setCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}
