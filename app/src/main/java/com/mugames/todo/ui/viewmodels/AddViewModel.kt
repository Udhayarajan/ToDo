package com.mugames.todo.ui.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.mugames.todo.data.SubTask
import com.mugames.todo.data.Task
import com.mugames.todo.data.TaskWithSubTasks
import com.mugames.todo.ui.activities.MainActivity.Companion.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.coroutineContext

class AddViewModel : ViewModel() {

    private val subTasksList: MutableList<SubTask> = listOf(
        SubTask(null, 0)
    ).toMutableList()

    var task: Task? = null

    private val _subTasks = MutableStateFlow(subTasksList)
    val subTasks: StateFlow<List<SubTask>> = _subTasks

    fun addSubTask() {
        subTasksList.add(SubTask(id = null, position = subTasksList.size))
    }

    fun removeSubTask() {
        if (subTasksList.size > 0) {
            subTasksList.removeLast()
        }
    }

    fun setSubTaskList(list: List<SubTask>) {
        subTasksList.clear()
        subTasksList.addAll(list)
    }

    fun replaceText(id: Int, newString: String) {
        subTasksList[id] = subTasksList[id].run {
            SubTask(this.id, this.position, newString, this.isDone, this.taskId)
        }
    }

    private fun getSubtasks(): List<SubTask> {
        val tasks = listOf<SubTask>().toMutableList()
        for (editTask in subTasksList) {
            if (editTask.text.isNotEmpty()) {
                val subTask = SubTask(
                    editTask.id,
                    tasks.size,
                    editTask.text,
                    editTask.isDone
                )
                tasks.add(subTask)
            }
        }
        return tasks
    }

    fun convertSubTasks(
        title: String,
        description: String,
        due: String,
    ): TaskWithSubTasks {
        return TaskWithSubTasks(
            Task(
                task?.id,
                title,
                description,
                task?.assigned ?: getTodayDate(),
                dateFromString(due),
                false
            ),
            getSubtasks()
        )
    }

    private fun getTodayDate(): Date {
        return Calendar.getInstance(Locale.getDefault()).time
    }

    private fun dateFromString(string: String): Date? {
        if (string == "") return null
        return SimpleDateFormat("dd-MMM-yy hh:mm:ss",
            Locale.getDefault()).parse(string.plus(" 23:59:59"))
    }

    fun swapSubTask(from: Int, to: Int) = Collections.swap(subTasksList, from, to)

    fun removeSubTaskAt(index: Int) {
        if (subTasksList.size > 0) subTasksList.removeAt(index)
    }

}