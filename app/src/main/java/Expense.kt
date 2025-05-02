package vcmsa.projects.personalbudgettingcorp

import android.graphics.Bitmap
import com.google.type.Date

//Expense data model representing an individual expense entry
data class Expense(
    val id: Long = 0,
    val amount: Double,
    val date: Date,
    val description: String,
    val receiptPath: String? = null,
    val categoryId: Long,
    val userId: Long,
    val photo: Bitmap? = null,
    val categoryld : Long,
    val userld: Long,
    val imagePath: String? = null
)