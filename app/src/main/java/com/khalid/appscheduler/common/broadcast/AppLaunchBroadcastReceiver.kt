package com.khalid.appscheduler.common.broadcast

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.repository.AppScheduleRepository
import com.khalid.appscheduler.repository.db.AppScheduleDB
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.utils.AppSchedulerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppLaunchBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "AppLaunchBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            AppScheduleLog.d(TAG, "[AppLaunchBroadcastReceiver] Received broadcast intent")
            val schedule = intent?.getParcelableExtra<AppLaunchSchedule>(AppSchedulerUtils.Companion.KEY_SCHEDULE_INFO)
            // TODO: 1. launch scheduled app
            if (context == null) {
                AppScheduleLog.d(TAG, "[AppLaunchBroadcastReceiver] Context is null")
                return
            }
            AppScheduleLog.d(TAG,
                "[AppLaunchBroadcastReceiver] Received launch app broadcast for: " +
                        "packageName: ${schedule?.packageName}, " +
                        "className: ${schedule?.className}"
            )
            schedule?.let {
                launchApp(context, schedule.packageName, schedule.className)
            }

            // TODO: 1. Store flag in shared preference for tracking user noti check
            AppSchedulerUtils.setUserNotiCheckStatus(context, true)
            //  TODO 2: update database for the launched app
            updateSchedule(context, schedule)
        } catch (ex: Exception) {
            AppScheduleLog.d(TAG, "[AppLaunchBroadcastReceiver] Exception: ${ex.message}")
        }
    }

    private fun updateSchedule(context: Context, schedule: AppLaunchSchedule?) {
        AppScheduleLog.d(TAG,
            "[updateSchedule] updating schedule in database, launch status: ${AppSchedulerUtils.Companion.LaunchSchedule.LAUNCH_SUCCESS}, " +
                    "id: ${schedule?.id}," +
                    "launchTime: {${schedule?.launchTime}"
        )
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppScheduleDB.Companion.getInstance(context)
            val dao = db.appScheduleDao()
            val repository = AppScheduleRepository(dao)
            schedule?.let {
                repository.updateLaunchStatus(
                    schedule.packageName,
                    schedule.className,
                    AppSchedulerUtils.Companion.LaunchSchedule.LAUNCH_SUCCESS.status,
                )
                repository.updateNotiStatus(
                    it.packageName,
                    it.className,
                    AppSchedulerUtils.Companion.ShowNotification.SHOWING_DONE.notiType
                )
            }
        }
    }

    fun launchApp(
        context: Context?,
        packageName: String,
        className: String
    ) {
        val launchIntent = Intent()
        launchIntent.apply {
            setComponent(ComponentName(packageName, className))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        AppScheduleLog.d(TAG, "[launchApp] Launching app: $packageName")
        context?.startActivity(launchIntent)
    }

}

