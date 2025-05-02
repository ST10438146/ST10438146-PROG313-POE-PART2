package vcmsa.projects.personalbudgettingcorp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import vcmsa.projects.personalbudgettingcorp.databinding.ActivityRegistrationBinding
import vcmsa.projects.personalbudgettingcorp.UserDao
import vcmsa.projects.personalbudgettingcorp.User

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
                        Toast.makeText(this@RegisterActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()) // Hash the password
                        val newUser = User(username = username, password = hashedPassword)
                        val userId = userDao.addUser(newUser)
                        if (userId != -1L) {
                            Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, "Registration failed", Toast.LENGTH_SHORT).show()
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