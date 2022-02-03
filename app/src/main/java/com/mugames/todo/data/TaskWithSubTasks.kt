package com.mugames.todo.data

import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import java.util.*

data class TaskWithSubTasks(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "task_id"
    )
    val subTasks: List<SubTask>
)

fun TaskWithSubTasks.getSortedSubTasks(): List<SubTask> {
    val  st = mutableListOf<SubTask>()
    st.addAll(subTasks)
    st.sort()
    return st
}

fun TaskWithSubTasks.getSubtaskDoneCount():Int{
    var count = 0
    for (task in subTasks){
        if(task.isDone) count++
    }
    return count
}