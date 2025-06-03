package com.khalid.appscheduler.common.broadcast

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.repository.AppScheduleRepository
import com.khalid.appscheduler.repository.db.AppScheduleDB
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.utils.AppSchedulerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowNotificationBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "ShowNotificationBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        // TODO: Handle received broadcast
        try {
            if(context == null) {
                AppScheduleLog.d(TAG, "[ShowNotificationBroadcastReceiver] Context is null")
                return
            }
            AppScheduleLog.d(TAG, "[ShowNotificationBroadcastReceiver] Received broadcast intent")
            val schedule = intent?.getParcelableExtra<AppLaunchSchedule>(AppSchedulerUtils.Companion.KEY_SCHEDULE_INFO)

            AppScheduleLog.d(
                TAG,
                "[ShowNotificationBroadcastReceiver] Received launch app broadcast for: " + "packageName: ${schedule?.packageName}, " + "className: ${schedule?.className}"
            )
            if (AppSchedulerUtils.isAppNotFound(context, schedule?.packageName) == false) {
                AppScheduleLog.d(TAG, "[ShowNotificationBroadcastReceiver] ${schedule?.packageName} is not found")
                Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
                return
            }
            AppScheduleLog.d(TAG, "[ShowNotificationBroadcastReceiver] ${schedule?.packageName} is installed on device")
            val pendingIntent = getPendingIntent(context, schedule)
            AppSchedulerUtils.showLaunchNotification(context, schedule?.launchTime?.time?.toInt() ?: 0, pendingIntent)
            updateNotificationStatus(context, schedule)
            // reset shared preference user noti flag
            AppScheduleLog.d(TAG, "[ShowNotificationBroadcastReceiver] Resetting user noti check flag, setting false")
            AppSchedulerUtils.setUserNotiCheckStatus(context, false)
            // broadcast intent after 1 minutes to check if user tapped on notification
            val time = schedule?.launchTime?.time?.toLong()?.let {
                it + 60000
            }
            if (time != null) {
                scheduleConfirmBroadcast(context, schedule)
            }
        }catch (e: Exception){
            AppScheduleLog.d(TAG, "[ShowNotificationBroadcastReceiver] Exception: ${e.message}")
        } catch (e: ClassNotFoundException) {
            AppScheduleLog.d(TAG, "[ShowNotificationBroadcastReceiver] Exception: ${e.message}")
        } catch (e: SecurityException) {
            AppScheduleLog.d(TAG, "[ShowNotificationBroadcastReceiver] Exception: ${e.message}")
        }
    }

    private fun updateNotificationStatus(context: Context, schedule: AppLaunchSchedule?) {
        AppScheduleLog.d(
            TAG,
            "[updateNotificationStatus] updating noti status for package: ${schedule?.packageName}, class: ${schedule?.className}"
        )
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppScheduleDB.Companion.getInstance(context)
            val dao = db.appScheduleDao()
            val repository = AppScheduleRepository(dao)
            schedule?.let {
                repository.updateNotiStatus(
                    it.packageName,
                    it.className,
                    AppSchedulerUtils.Companion.ShowNotification.SHOWING.notiType
                )
            }
        }
    }
    private fun scheduleConfirmBroadcast(
        context: Context,
        schedule: AppLaunchSchedule?
    ) {
        // Schedule broadcast intent after 1 minutes to check if user tapped on notification
        val intent = Intent(context, AutoDismissNotiBroadcastReceiver::class.java).apply {
            putExtra(AppSchedulerUtils.Companion.KEY_SCHEDULE_INFO, schedule)
        }
        val launchTime = schedule?.launchTime?.time?.toLong()?.let {
            it + 60000
        }
        val requestCode = schedule?.launchTime?.time?.toInt()?.plus(1000)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        AppScheduleLog.d(TAG,"Scheduling another broadcast intent for: ${schedule?.packageName} to check user tapped on notification or not")
        AppSchedulerUtils.schedule(context, schedule, pendingIntent, launchTime)
    }

    private fun getPendingIntent(
        context: Context?,
        schedule: AppLaunchSchedule?
    ): PendingIntent {
        val broadcastIntent = Intent(context, AppLaunchBroadcastReceiver::class.java).apply {
            putExtra(AppSchedulerUtils.Companion.KEY_SCHEDULE_INFO, schedule)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule?.launchTime?.time?.toInt() ?: 0,
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent
    }

}