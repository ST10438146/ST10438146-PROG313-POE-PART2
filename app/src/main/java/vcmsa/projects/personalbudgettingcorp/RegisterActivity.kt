package vcmsa.projects.personalbudgettingcorp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditTexts: EditText
    private lateinit var passwordEditTexts: EditText
    private lateinit var registerButtons: Button
    private lateinit var dbHelper: BudgetDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditTexts = findViewById(R.id.usernameEditText)
        passwordEditTexts = findViewById(R.id.passwordEditText)
        registerButtons = findViewById(R.id.registerButton)
        dbHelper = BudgetDatabase(this)

        registerButtons.setOnClickListener {
            val username = usernameEditTexts.text.toString().trim()
            val password = passwordEditTexts.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = dbHelper.addUser(username, password)
            if (result > 0) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                finish() // Optionally, go back to login screen
            } else {
                Toast.makeText(this, "Registration failed. Username might be taken.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

