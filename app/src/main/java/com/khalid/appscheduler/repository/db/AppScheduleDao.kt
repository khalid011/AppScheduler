package com.khalid.appscheduler.repository.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.repository.model.InstalledAppInfo
import java.util.Date

@Dao
interface AppScheduleDao {

    @Query("SELECT * FROM app_launch_schedule")
    suspend fun getAllSchedules(): List<AppLaunchSchedule>

    @Query("SELECT COUNT(*) FROM app_launch_schedule WHERE launchTime = :launchTime")
    suspend fun getScheduleByLaunchTime(launchTime: Date) : Int

    @Insert
    suspend fun insertSchedule(schedule: AppLaunchSchedule)

    @Update
    suspend fun updateSchedule(schedule: AppLaunchSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: AppLaunchSchedule) : Int

    @Query("SELECT * FROM installed_app_info")
    suspend fun getAllInstalledAppInfo() : List<InstalledAppInfo>

    @Insert
    suspend fun insertInstalledAppInfo(installedAppInfo: InstalledAppInfo)

}