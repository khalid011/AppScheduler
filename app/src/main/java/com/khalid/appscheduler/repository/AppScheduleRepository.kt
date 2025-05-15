package com.khalid.appscheduler.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.repository.db.AppScheduleDao
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.repository.model.InstalledAppInfo
import com.khalid.appscheduler.utils.AppSchedulerUtils
import java.util.Date

class AppScheduleRepository(private val appScheduleDao: AppScheduleDao) {

    private val TAG = "AppScheduleRepository"


    suspend fun getScheduleByLaunchTime(launchTime: Date): Int {
        try {
            return appScheduleDao.getScheduleByLaunchTime(launchTime)
        } catch (e: Exception) {
            AppScheduleLog.d(TAG, "[getScheduleByLaunchTime] error: ${e.message}")
        }
        return AppSchedulerUtils.DB_NO_ENTRY_FOUND // default: returning 0 as no entry found in db
    }

    suspend fun getAllInstalledAppInfo(): List<InstalledAppInfo> {
        try {
            return appScheduleDao.getAllInstalledAppInfo()
        } catch (e: Exception) {
            AppScheduleLog.d(TAG, "[getAllInstalledAppInfo] error: ${e.message}")
        }
        return emptyList()
    }

    suspend fun insertInstalledAppInfo(installedAppInfo: InstalledAppInfo) {
        try {
            appScheduleDao.insertInstalledAppInfo(installedAppInfo)
        } catch (e: Exception) {
            AppScheduleLog.d(TAG, "[insertInstalledAppInfo] error: ${e.message}")
        }
    }
    suspend fun getAllSchedules(): List<AppLaunchSchedule> {
        try {
            return appScheduleDao.getAllSchedules()
        } catch (e: Exception) {
            AppScheduleLog.d(TAG, "[getAllSchedules] error: ${e.message}")
        }
        return emptyList()
    }

    suspend fun insertSchedule(schedule: AppLaunchSchedule) : Int {
        try {
            Log.d(TAG, "[insertSchedule] inserting...")
            appScheduleDao.insertSchedule(schedule)
        } catch (e: Exception) {
            AppScheduleLog.d(TAG, "[insertSchedule] error: ${e.message}")
            if(e is SQLiteConstraintException) {
                return AppSchedulerUtils.DUPLICATE_LAUNCH_TIME // duplicate launch time not possible
            }
        }
        return AppSchedulerUtils.DB_CRUD_SUCCESS
    }

    suspend fun updateSchedule(schedule: AppLaunchSchedule) : Int? {
        AppScheduleLog.d(TAG, "[updateSchedule] updating...")
        try {
            appScheduleDao.updateSchedule(schedule)
        } catch (e: Exception) {
            AppScheduleLog.d(TAG, "[updateSchedule] error: ${e.message}")
            if(e is SQLiteConstraintException) {
                return AppSchedulerUtils.DUPLICATE_LAUNCH_TIME // duplicate launch time not possible
            }
        }
        AppScheduleLog.d(TAG, "[updateSchedule] success")
        return AppSchedulerUtils.DB_CRUD_SUCCESS
    }

    suspend fun deleteSchedule(schedule: AppLaunchSchedule) : Int {
        try {
            return appScheduleDao.deleteSchedule(schedule)
        } catch (e: Exception) {
            AppScheduleLog.d(TAG, "[deleteSchedule] error: ${e.message}")
        }
        return 0 // delete failure. No item is deleted
    }
}