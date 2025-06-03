package com.khalid.appscheduler.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.repository.model.InstalledAppInfo
import com.khalid.appscheduler.repository.utils.DateConverter

@Database(entities = [AppLaunchSchedule::class, InstalledAppInfo::class], version = 5)
@TypeConverters(DateConverter::class)
abstract class AppScheduleDB : RoomDatabase() {
    abstract fun appScheduleDao(): AppScheduleDao
    companion object {
        private var instance: AppScheduleDB? = null
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("CREATE TABLE IF NOT EXISTS `installed_app_info` (id INTEGER PRIMARY KEY AUTOINCREMENT, `packageName` TEXT NOT NULL, `className` TEXT NOT NULL)")
//            }
//        }

        fun getInstance(context: Context): AppScheduleDB {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppScheduleDB::class.java,
                    "app_schedule_db"
                )//.addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(true)
                .build()
            }
            return instance as AppScheduleDB
        }
    }
}