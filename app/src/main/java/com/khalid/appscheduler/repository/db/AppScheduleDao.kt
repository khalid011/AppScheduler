package com.khalid.appscheduler.repository.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.repository.model.InstalledAppInfo
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AppScheduleDao {

//    @Query("SELECT * FROM app_launch_schedule")
//    suspend fun getAllSchedules(): LiveData<List<AppLaunchSchedule>>

    @Query("SELECT * FROM app_launch_schedule")
    fun getAllSchedules(): Flow<List<AppLaunchSchedule>>

    @Query("SELECT * FROM app_launch_schedule WHERE launchStatus = 0 ORDER BY launchTime ASC")
    fun getUpcomingSchedules(): Flow<List<AppLaunchSchedule>>

    @Query("SELECT * FROM app_launch_schedule WHERE launchStatus = 1 ORDER BY launchTime ASC")
    fun getPreviousSuccessfulSchedules(): Flow<List<AppLaunchSchedule>>

    @Query("SELECT COUNT(*) FROM app_launch_schedule WHERE launchTime = :launchTime")
    suspend fun getScheduleByLaunchTime(launchTime: Date) : Int

    @Insert
    suspend fun insertSchedule(schedule: AppLaunchSchedule)

    @Update
    suspend fun updateSchedule(schedule: AppLaunchSchedule)

    @Query("UPDATE app_launch_schedule SET showNotification = :notiStatus WHERE packageName = :packageName AND className = :className")
    fun updateNotiStatus(packageName: String, className: String, notiStatus: Int)

    @Query("UPDATE app_launch_schedule SET launchStatus = :launchStatus WHERE packageName = :packageName AND className = :className")
    suspend fun updateLaunchStatus(packageName: String, className: String, launchStatus: Int)

    @Query("DELETE FROM app_launch_schedule WHERE packageName = :packageName AND className = :className")
    suspend fun deleteScheduleByPackageAndClass(packageName: String, className: String) : Int

    @Delete
    suspend fun deleteSchedule(schedule: AppLaunchSchedule) : Int

    @Query("SELECT * FROM installed_app_info")
    suspend fun getAllInstalledAppInfo() : List<InstalledAppInfo>

    @Insert
    suspend fun insertInstalledAppInfo(installedAppInfo: InstalledAppInfo)

}