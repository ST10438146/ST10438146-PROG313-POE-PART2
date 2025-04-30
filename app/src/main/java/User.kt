package vcmsa.projects.personalbudgettingcorp
//User data model representing a user in the app
data class User(
    val id: Long = 0,
    val username: String,
    val password: String
)