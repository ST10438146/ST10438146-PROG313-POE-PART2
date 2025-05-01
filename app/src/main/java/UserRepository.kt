package vcmsa.projects.personalbudgettingcorp

import android.util.Log

//Repository class for User-related operations
class UserRepository(private val userDao: UserDao) {
    private val TAG = "UserRepository"

    //Registers a new user
    fun registerUser(username: String, password: String): User? {
        try {
            // Checks if user already exists
            val existingUser = userDao.getUserByUsername(username)
            if (existingUser != null) {
                Log.d(TAG, "User already exists: $username")
                return null
            }

            // Creates new user
            val newUser = User(username = username, password = password)
            val userId = userDao.insert(newUser)

            if (userId == -1L) {
                Log.e(TAG, "Failed to register user: $username")
                return null
            }

            return newUser.copy(id = userId)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during user registration: ${e.message}")
            return null
        }
    }

    //Logs in a user
    fun loginUser(username: String, password: String): User? {
        try {
            return userDao.authenticate(username, password)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during user login: ${e.message}")
            return null
        }
    }
}