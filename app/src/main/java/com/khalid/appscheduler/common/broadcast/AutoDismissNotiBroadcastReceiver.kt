package com.khalid.appscheduler.common.broadcast

import android.content.BroadcastReceiver
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

class AutoDismissNotiBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "AutoDismissNotiBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        AppScheduleLog.d(TAG, "[AutoDismissNotiBroadcastReceiver] Received broadcast intent")
        // TODO: Handle received broadcast
        val schedule = intent?.getParcelableExtra<AppLaunchSchedule>(AppSchedulerUtils.Companion.KEY_SCHEDULE_INFO)
        val sharedPref = context?.getSharedPreferences(
            AppSchedulerUtils.SHARED_PREF_APP_SCHEDULE,
            Context.MODE_PRIVATE
        )
        val notiCheckFlag = sharedPref?.getBoolean(AppSchedulerUtils.KEY_USER_NOTI_CHECK, false)
        if(notiCheckFlag == false) {
            // Remove entry from database
            AppScheduleLog.d(TAG, "[AutoDismissNotiBroadcastReceiver] User did not tap on notification in schedule time, removing schedule from database")
            removeSchedule(context, schedule)
        } else {
            AppScheduleLog.d(TAG, "[AutoDismissNotiBroadcastReceiver] User tapped on notification")
        }
    }

    private fun removeSchedule(context: Context, schedule: AppLaunchSchedule?) {
        AppScheduleLog.d(TAG, "[removeSchedule] Removing schedule from database")
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppScheduleDB.Companion.getInstance(context)
            val dao = db.appScheduleDao()
            val repository = AppScheduleRepository(dao)
            schedule?.let {
                repository.deleteScheduleByPackageAndClass(it.packageName, it.className)
            }
        }
    }

}