package vcmsa.projects.personalbudgettingcorp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDao(this)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val existingUser = userDao.getUserByUsername(username)
                withContext(Dispatchers.Main) {
                    if (existingUser != null) {
                        Toast.makeText(this@RegistrationActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        val newUser = User(username = username, password = password) // WARNING: Insecure!
                        val userId = userDao.addUser(newUser)
                        if (userId != -1L) {
                            Toast.makeText(this@RegistrationActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                            finish() // Go back to LoginActivity
                        } else {
                            Toast.makeText(this@RegistrationActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            finish() // Go back to LoginActivity
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userDao.close()
    }
}