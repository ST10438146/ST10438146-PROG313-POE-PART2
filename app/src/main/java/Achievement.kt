package vcmsa.projects.personalbudgettingcorp
//Achievement data model for gamification features
data class Achievement(
    val id: Long = 0,
    val name: String,
    val description: String,
    val unlocked: Boolean = false,
    val unlockDate: String? = null,
    val userId: Long? = null
)
