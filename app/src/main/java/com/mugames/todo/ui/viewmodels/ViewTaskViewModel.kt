package com.mugames.todo.ui.viewmodels

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mugames.todo.data.SubTask
import com.mugames.todo.data.Task
import com.mugames.todo.data.TaskWithSubTasks
import kotlinx.coroutines.launch

class ViewTaskViewModel : ViewModel() {
    lateinit var taskWithSubTasks: TaskWithSubTasks

    fun updateSubTask(position: Int, status: Boolean): SubTask {
        taskWithSubTasks.subTasks[position].isDone = status
        return taskWithSubTasks.subTasks[position]
    }
}