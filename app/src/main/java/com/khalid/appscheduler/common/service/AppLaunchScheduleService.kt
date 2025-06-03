package com.khalid.appscheduler.common.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.utils.AppSchedulerUtils

class AppLaunchScheduleService : Service() {

    private val TAG = "AppLaunchScheduleService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val packageName = intent?.getStringExtra(AppSchedulerUtils.Companion.KEY_SELECTED_APP_PACKAGE)
            val className = intent?.getStringExtra(AppSchedulerUtils.Companion.KEY_SELECTED_APP_CLASS)
            AppScheduleLog.d(TAG, "[AppLaunchScheduleService] starting foreground service for: " + "packageName: ${packageName}, " + "className: $className")
            if (isAppNotFound(packageName) == false) {
                AppScheduleLog.d(TAG, "[AppLaunchBroadcastReceiver] $packageName is not found")
                return START_NOT_STICKY
            }
            AppScheduleLog.d(TAG, "[AppLaunchBroadcastReceiver] $packageName is installed on device")
//            startForeground(1, AppSchedulerUtils.createAppLaunchNotification(this))
            launchApp(packageName, className)
            stopSelf()
            return START_NOT_STICKY
        } catch (exception: Exception) {
            AppScheduleLog.d(TAG, "[AppLaunchBroadcastReceiver] Error launching app: ${exception.message}")
        }
        return START_NOT_STICKY
    }

    private fun launchApp(packageName: String?, className: String?) {
        val launchIntent = Intent()
        launchIntent.apply {
            setComponent(ComponentName(packageName ?: "", className ?: ""))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        AppScheduleLog.d(TAG, "[launchApp] Launching app: $packageName")
        startActivity(launchIntent)
    }

    private fun isAppNotFound(packageName: String?): Boolean {
        val isInstalled = packageManager?.getLaunchIntentForPackage(packageName ?: "") != null
        return isInstalled
    }

    override fun onBind(intent: Intent?): IBinder? = null
}