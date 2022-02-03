package com.mugames.todo.data

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class DateConverters {
    @TypeConverter
    fun fromLong(milli:Long?):Date?{
        if(milli==null) return null
        return Date(milli)
    }
    @TypeConverter
    fun toLong(date: Date?):Long?{
        if(date==null) return null
        return date.time
    }
}