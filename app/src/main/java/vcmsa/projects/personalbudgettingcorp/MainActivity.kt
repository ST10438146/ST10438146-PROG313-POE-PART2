package vcmsa.projects.personalbudgettingcorp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNavigation

        // Load the HomeFragment as the initial fragment
        loadFragment(HomeFragment())

        // Set up the listener for bottom navigation item clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_expenses -> {
                    loadFragment(ExpenseFragment())
                    true
                }
                R.id.navigation_categories -> {
                    loadFragment(CategoriesFragment())
                    true
                }
                R.id.navigation_budget -> {
                    loadFragment(BudgetFragment())
                    true
                }
                R.id.navigation_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}