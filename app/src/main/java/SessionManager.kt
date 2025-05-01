package vcmsa.projects.personalbudgettingcorp

import android.content.Context
import android.content.SharedPreferences

//Session manager for handling user login sessions
class SessionManager(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    companion object {
        private const val PREF_NAME = "BudgetTrackerPref"
        private const val IS_LOGIN = "IsLoggedIn"
        private const val KEY_ID = "userId"
        private const val KEY_USERNAME = "username"
    }

    //Creates login session
    fun createLoginSession(userId: Long, username: String) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putLong(KEY_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.apply() // Use apply for background commit
    }

    //Gets user details
    fun getUserDetails(): Map<String, Any> {
        val user = HashMap<String, Any>()
        user[KEY_ID] = pref.getLong(KEY_ID, 0)
        user[KEY_USERNAME] = pref.getString(KEY_USERNAME, "") ?: ""
        return user
    }

    //Gets user ID
    fun getUserId(): Long {
        return pref.getLong(KEY_ID, 0)
    }

    //Checks login status
    fun isLoggedIn(): Boolean {
        return pref.getBoolean(IS_LOGIN, false)
    }

    //Clears session details
    fun logoutUser() {
        editor.clear()
        editor.apply() // Use apply for background commit
    }
}