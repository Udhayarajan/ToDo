package com.mugames.todo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SubTask::class,Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getTaskDao(): TaskDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "data_base"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}