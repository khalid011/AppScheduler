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
import com.khalid.appscheduler.common.broadcast.ShowNotificationBroadcastReceiver
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.repository.AppScheduleRepository
import com.khalid.appscheduler.repository.db.AppScheduleDB
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.utils.AppSchedulerUtils
import com.khalid.appscheduler.utils.AppSchedulerUtils.Companion.getPhoneAlarmManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ScheduleAppViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ScheduleAppViewModel"
    private var repository: AppScheduleRepository? = null
    private val _deleteSuccessStatus = MutableLiveData<Boolean>()
    val deleteSuccessStatus : LiveData<Boolean> = _deleteSuccessStatus

    private val _duplicateLaunchTimeFound = MutableLiveData<Boolean>()
    val duplicateLaunchTimeFound: MutableLiveData<Boolean> = _duplicateLaunchTimeFound

    private val _scheduleAppState = MutableStateFlow<List<AppLaunchSchedule>>(emptyList())
    val scheduleAppState: StateFlow<List<AppLaunchSchedule>> = _scheduleAppState

    private val _previousScheduleAppState = MutableStateFlow<List<AppLaunchSchedule>>(emptyList())
    val previousScheduleAppState: StateFlow<List<AppLaunchSchedule>> = _previousScheduleAppState

    init {
        val context = getApplication<Application>().applicationContext
        val db = AppScheduleDB.Companion.getInstance(getApplication())
        val dao = db.appScheduleDao()
        repository = AppScheduleRepository(dao)
        viewModelScope.launch {
            AppScheduleLog.d(TAG, "[init] get all schedules from repository")
            repository?.schedules?.collect {
                _scheduleAppState.value = it
            }
        }
        viewModelScope.launch {
            AppScheduleLog.d(TAG, "[init] get all previous schedules from repository")
            repository?.previousSchedule?.collect {
                _previousScheduleAppState.value = it
            }
        }
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

    private fun getPendingIntent(schedule: AppLaunchSchedule) : PendingIntent {
        val intent = Intent(getApplication<Application>(), ShowNotificationBroadcastReceiver::class.java).apply {
            putExtra(AppSchedulerUtils.Companion.KEY_SCHEDULE_INFO, schedule)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            getApplication<Application>(),
            schedule.launchTime?.time?.toInt() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        AppScheduleLog.d(TAG, "[getPendingIntent] pending intent: $pendingIntent")
        return pendingIntent
    }

    fun cancelAppLaunch(schedule: AppLaunchSchedule) {
        AppScheduleLog.d(
            TAG,
            "[cancelAppLaunch] packageName: ${schedule.packageName}, className: ${schedule.className}, launchTime: ${schedule.launchTime}"
        )
        try {
            val pendingIntent = getPendingIntent(schedule)
            AppSchedulerUtils.getPhoneAlarmManager(getApplication<Application>())
                ?.cancel(pendingIntent)
        } catch (exception: Exception) {
            AppScheduleLog.d(
                TAG,
                "[cancelAppLaunch]: Error cancelling app schedule: ${exception.message}"
            )
        }
    }

    fun scheduleAppLaunch(schedule: AppLaunchSchedule) {
        try {
            val pendingIntent = getPendingIntent(schedule)
            AppSchedulerUtils.schedule(getApplication<Application>(), schedule, pendingIntent)
        } catch (exception: Exception) {
            AppScheduleLog.d(TAG, "[scheduleAppLaunch]: Error scheduling app launch: ${exception.message}")
        }
    }


//    suspend fun getAllSchedules() = repository?.getAllSchedules()

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