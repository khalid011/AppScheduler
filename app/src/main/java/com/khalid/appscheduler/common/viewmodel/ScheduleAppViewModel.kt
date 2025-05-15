package com.khalid.appscheduler.common.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.khalid.appscheduler.common.broadcast.AppLaunchBroadcastReceiver
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.repository.AppScheduleRepository
import com.khalid.appscheduler.repository.db.AppScheduleDB
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.utils.AppSchedulerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Date

class ScheduleAppViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ScheduleAppViewModel"
    private var repository: AppScheduleRepository? = null
    private val _deleteSuccessStatus = MutableLiveData<Boolean>()
    val deleteSuccessStatus : LiveData<Boolean> = _deleteSuccessStatus

    private val _duplicateLaunchTimeFound = MutableLiveData<Boolean>()
    val duplicateLaunchTimeFound: MutableLiveData<Boolean> = _duplicateLaunchTimeFound

    init {
        val context = getApplication<Application>().applicationContext
        val db = AppScheduleDB.Companion.getInstance(getApplication())
        val dao = db.appScheduleDao()
        repository = AppScheduleRepository(dao)
    }

    fun getScheduleByLaunchTime(launchTime: Date): Int {
        var scheduleCount = 0
        viewModelScope.launch(Dispatchers.IO) {
            scheduleCount = repository?.getScheduleByLaunchTime(launchTime) ?: 0
        }
        AppScheduleLog.d(TAG, "[getScheduleByLaunchTime] schedule count: $scheduleCount")
        return scheduleCount
    }

    fun cancelScheduledAppLaunch(packageName: String, className: String, launchTime: Long) {

    }

    private fun getPhoneAlarmManager() : AlarmManager? {
        val alarmManager : AlarmManager? = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        return alarmManager
    }

    private fun getPendingIntent(packageName: String, className: String, launchTime: Long) : PendingIntent {
        val intent = Intent(getApplication<Application>(), AppLaunchBroadcastReceiver::class.java).apply {
            putExtra(AppSchedulerUtils.Companion.KEY_SELECTED_APP_PACKAGE, packageName)
            putExtra(AppSchedulerUtils.Companion.KEY_SELECTED_APP_CLASS, className)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication<Application>(),
            launchTime.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        AppScheduleLog.d(TAG, "[getPendingIntent] pending intent: $pendingIntent")
        return pendingIntent
    }

    fun cancelAppLaunch(packageName: String, className: String, launchTime: Long) {
        AppScheduleLog.d(TAG, "[cancelAppLaunch] packageName: $packageName, className: $className, launchTime: $launchTime")
        try {
            val pendingIntent = getPendingIntent(packageName, className, launchTime)
            getPhoneAlarmManager()?.cancel(pendingIntent)
        } catch (exception: Exception) {
            AppScheduleLog.d(TAG, "[cancelAppLaunch]: Error cancelling app schedule: ${exception.message}")
        }
    }

    fun scheduleAppLaunch(packageName: String, className: String, launchTime: Long) {
        try {
            val pendingIntent = getPendingIntent(packageName, className, launchTime)
            val alarmManager = getPhoneAlarmManager()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val isGrantedPermission = alarmManager?.canScheduleExactAlarms() == true
                if(isGrantedPermission == true) {
                    AppScheduleLog.d(TAG, "[scheduleAppLaunch]: $packageName launch is scheduled successfully by setExact api")
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, launchTime, pendingIntent)
                } else {
                    AppScheduleLog.d(TAG, "[scheduleAppLaunch]: $packageName launch permission is not granted by system")
                }
            } else {
                AppScheduleLog.d(TAG, "[scheduleAppLaunch]: $packageName launch is scheduled successfully by set api")
                alarmManager?.set(AlarmManager.RTC_WAKEUP, launchTime, pendingIntent)
            }
        } catch (exception: Exception) {
            AppScheduleLog.d(TAG, "[scheduleAppLaunch]: Error scheduling app launch: ${exception.message}")
        }
    }

    suspend fun getAllSchedules() = repository?.getAllSchedules()

    suspend fun insertSchedule(schedule: AppLaunchSchedule) : Int? {
        val result = repository?.insertSchedule(schedule)
        AppScheduleLog.d(TAG, "[insertSchedule] result: $result")
        return result
    }

    suspend fun updateSchedule(schedule: AppLaunchSchedule) : Int? = repository?.updateSchedule(schedule)

    suspend fun deleteSchedule(schedule: AppLaunchSchedule) {

        viewModelScope.launch(Dispatchers.Main) {

            val deferred = CoroutineScope(Dispatchers.IO).async {
                repository?.deleteSchedule(schedule)
            }
            val deleteItemCount = deferred.await() ?: 0

            if(deleteItemCount > 0) {
                _deleteSuccessStatus.value = true
            } else {
                _deleteSuccessStatus.value = false
            }
        }
    }

}