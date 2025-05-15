package com.khalid.appscheduler.common.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.common.service.AppLaunchScheduleService
import com.khalid.appscheduler.utils.AppSchedulerUtils

class AppLaunchBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "AppLaunchBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        // TODO: Handle received broadcast
        try {
            AppScheduleLog.d(TAG, "[AppLaunchBroadcastReceiver] Received broadcast intent")
            val packageName = intent?.getStringExtra(AppSchedulerUtils.Companion.KEY_SELECTED_APP_PACKAGE)
            val className = intent?.getStringExtra(AppSchedulerUtils.Companion.KEY_SELECTED_APP_CLASS)
            AppScheduleLog.d(TAG, "[AppLaunchBroadcastReceiver] Received launch app broadcast for: " + "packageName: ${packageName}, " + "className: $className")
            val intent = Intent(context, AppLaunchScheduleService::class.java)
            intent.apply {
                putExtra(AppSchedulerUtils.Companion.KEY_SELECTED_APP_PACKAGE, packageName)
                putExtra(AppSchedulerUtils.Companion.KEY_SELECTED_APP_CLASS, className)
            }
            context?.startForegroundService(intent)
        }catch (e: Exception){
            AppScheduleLog.d(TAG, "[AppLaunchBroadcastReceiver] Exception: ${e.message}")
        }
    }
}