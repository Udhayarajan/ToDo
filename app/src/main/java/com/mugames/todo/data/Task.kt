package com.mugames.todo.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mugames.todo.R
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "tasks_table")
@TypeConverters(DateConverters::class)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,
    @ColumnInfo(name = "title")
    val tile: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "date_assigned")
    val assigned: Date,
    @ColumnInfo(name = "date_due")
    val due: Date?,
    @ColumnInfo(name = "is_completed")
    var isCompleted: Boolean
)


private fun dateFormat(date: Date?): String? {
    if (date == null) return null
    val dateFormat = SimpleDateFormat("dd-MMM-yy", Locale.getDefault())
    return dateFormat.format(date)
}

fun Task.getDueDate(context: Context): String = context.getString(R.string.due_on).plus(" ").plus(dateFormat(due) ?: run {
    context.getString(R.string.not_assigned)
})

fun Task.dateFormat(): String = dateFormat(due) ?: ""

fun Task.getAssignedDate(context: Context): String = context.getString(R.string.assigned_on).plus(" ") + dateFormat(assigned)
