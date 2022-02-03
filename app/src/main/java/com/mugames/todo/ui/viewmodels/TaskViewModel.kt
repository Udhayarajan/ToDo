package com.mugames.todo.ui.viewmodels

import android.os.Parcelable
import androidx.lifecycle.*
import com.mugames.todo.R
import com.mugames.todo.data.SubTask
import com.mugames.todo.data.TaskDao
import com.mugames.todo.data.TaskWithSubTasks
import kotlinx.coroutines.launch

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    var sortingId: Int = R.id.sort_assigned
    var adapterSize: Int = -1
    var selectedId: Int = -1
    var taskListState: Parcelable? = null
    var snackBarMessage: String? = null

    /**
     * Values need to be displayed in ToDo
     */
    fun getToDoTask(): LiveData<List<TaskWithSubTasks>> {
        return taskDao.getToDoTask(System.currentTimeMillis()).asLiveData()
    }


    /**
     * Data needs to be populated in OverDue
     */
    fun getOverDueTask(): LiveData<List<TaskWithSubTasks>> =
        taskDao.getOverDueTask(System.currentTimeMillis()).asLiveData()

    /**
     * Data needs to be populated in Completed
     */
    fun getDoneTask(): LiveData<List<TaskWithSubTasks>> = taskDao.getCompletedTask().asLiveData()

    fun getTaskAt(taskID: Int): LiveData<TaskWithSubTasks> = taskDao.getTask(taskID).asLiveData()


    fun addTask(tasks: TaskWithSubTasks) {
        viewModelScope.launch {
            val id = taskDao.addTask(tasks.task)
            for (st in tasks.subTasks) {
                st.taskId = id.toInt()
                taskDao.addSubTasks(st)
            }
        }
    }

    fun updateSubTask(subTask: SubTask) = viewModelScope.launch { taskDao.addSubTasks(subTask) }

    fun deleteTask(tasks: TaskWithSubTasks) {
        viewModelScope.launch {
            taskDao.removeTask(tasks.task)
            for (s in tasks.subTasks) {
                taskDao.removeSubTask(s)
            }
        }
    }

    fun deleteAllTask() {
        viewModelScope.launch {
            taskDao.removeAllTask()
            taskDao.removeAllSubTask()
        }
    }

}

class TaskViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown class passed")
    }

}