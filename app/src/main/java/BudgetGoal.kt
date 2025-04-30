package vcmsa.projects.personalbudgettingcorp
//BudgetGoal data model representing a monthly budget goal
data class BudgetGoal(
    val id: Long = 0,
    val amount: Double,
    val month: Int,
    val year: Int,
    val userId: Long
)