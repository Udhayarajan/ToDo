package com.mugames.todo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subtasks_table")
data class SubTask(
    @PrimaryKey(autoGenerate = true)
    val id:Int?,
    var position:Int,
    var text:String = "",
    @ColumnInfo(name = "is_done")
    var isDone:Boolean = false,
    @ColumnInfo(name = "task_id")
    var taskId:Int = -1,
):Comparable<SubTask> {
    override fun compareTo(other: SubTask): Int {
        return position - other.position
    }
}
