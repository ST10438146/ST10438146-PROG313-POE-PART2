package vcmsa.projects.personalbudgettingcorp
//Expense data model representing an individual expense entry
data class Expense(
    val id: Long = 0,
    val amount: Double,
    val date: String,
    val description: String,
    val receiptPath: String? = null,
    val categoryId: Long,
    val userId: Long
)