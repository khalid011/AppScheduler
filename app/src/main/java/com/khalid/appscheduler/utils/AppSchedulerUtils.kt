package com.khalid.appscheduler.utils

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import androidx.core.app.NotificationCompat
import com.khalid.appscheduler.R
import java.util.Date
import java.util.Locale

class AppSchedulerUtils {
    companion object {
        const val RESULT_SUCCESS = 1
        const val RESULT_CANCELED = 0
        const val KEY_SELECTED_APP = "SELECTED_APP"
        const val KEY_SELECTED_APP_PACKAGE = "SELECTED_APP_PACKAGE"
        const val KEY_SELECTED_APP_CLASS = "SELECTED_APP_CLASS"
        const val KEY_MODIFY_SCHEDULE = "MODIFY_SCHEDULE"
        const val KEY_DELETE_SCHEDULE = "DELETE_SCHEDULE"
        const val KEY_ADD_SCHEDULE = "ADD_SCHEDULE"
        const val KEY_PHONE_APP_SAVE_STATUS = "PHONE_APP_SAVE_STATUS"
        const val KEY_BROADCAST_REQUEST_CODE = "KEY_BROADCAST_REQUEST_CODE"
        const val SHARED_PREF_APP_SCHEDULE = "com.khalid.appscheduler"
        const val STATUS_APP_LAUNCH_SCHEDULED = 0
        const val STATUS_APP_LAUNCH_SUCCESS = 1
        const val STATUS_APP_LAUNCH_FAILED = -1
        const val INVALID_PRIMARY_KEY = -1
        const val INVALID_INDEX = -1
        const val DUPLICATE_LAUNCH_TIME = -1
        const val SEND_BROADCAST_REQUEST_LAUNCH_APP = 0
        const val DB_NO_ENTRY_FOUND = 0
        const val DB_CRUD_SUCCESS = 0

        fun getAppIcon(context: Context, packageName: String, className: String) : Drawable? {
            val appIcon = context.packageManager.getActivityIcon(ComponentName(packageName, className))
            return appIcon
        }

        fun getAppTitle(context: Context, packageName: String, className: String) : String {
            val component = ComponentName(packageName, className)
            val appInfo = context.packageManager.getActivityInfo(component, 0)
            return appInfo.loadLabel(context.packageManager).toString()
        }

        fun getLaunchTime(date: Date?) : String {
            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val formattedDate = formatter.format(date)
            return formatter.format(date)
        }

        fun createAppLaunchNotification(context: Context) : Notification {
            val notification = NotificationCompat.Builder(context, "1001")
                .setContentTitle("App Scheduler")
                .setContentText("Schedule app launch for apps in your phone")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            return notification
        }
    }
}