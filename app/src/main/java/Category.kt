package vcmsa.projects.personalbudgettingcorp
//Category data model representing an expense category
data class Category(
    val id: Long = 0,
    val name: String,
    val color: String,
    val budgetLimit: Double = 0.0,
    val userId: Long? = null
)