package com.mugames.todo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun addTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun addSubTasks(subTask: SubTask)


    @Query("SELECT * FROM tasks_table WHERE id = :id")
    fun getTask(id: Int): Flow<TaskWithSubTasks>

    @Query("SELECT * FROM tasks_table WHERE is_completed = 0 AND (date_due>=:time OR date_due IS NULL) ORDER BY date_assigned desc")
    fun getToDoTask(time: Long): Flow<List<TaskWithSubTasks>>

    @Query("SELECT * FROM tasks_table WHERE is_completed = 0 AND date_due < :time ORDER BY date_due")
    fun getOverDueTask(time: Long): Flow<List<TaskWithSubTasks>>

    @Query("SELECT * FROM tasks_table WHERE is_completed = 1 ORDER BY date_assigned desc")
    fun getCompletedTask(): Flow<List<TaskWithSubTasks>>

    @Delete
    suspend fun removeTask(task: Task)

    @Delete
    suspend fun removeSubTask(subTask: SubTask)

    @Query("DELETE FROM tasks_table")
    suspend fun removeAllTask()

    @Query("DELETE FROM subtasks_table")
    suspend fun removeAllSubTask()

}