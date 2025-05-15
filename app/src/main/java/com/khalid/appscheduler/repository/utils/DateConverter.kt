package com.khalid.appscheduler.repository.utils

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {

    @TypeConverter
    fun fromLongToDate(valueInLong: Long?): Date? {
        return valueInLong?.let { Date(it) }
    }

    @TypeConverter
    fun fromDateToLong(valueInDate: Date?): Long? {
        return valueInDate?.time
    }

}