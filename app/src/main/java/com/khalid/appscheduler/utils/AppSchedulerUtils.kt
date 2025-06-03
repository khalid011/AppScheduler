package com.khalid.appscheduler.utils

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.khalid.appscheduler.R
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import java.util.Date
import java.util.Locale

class AppSchedulerUtils {
    companion object {
        const val TAG = "AppSchedulerUtils"
        const val RESULT_SUCCESS = 1
        const val RESULT_CANCELED = 0
        const val CHANNEL_NAME = "SCHEDULED_APP_NOTIFICATION"
        const val KEY_SCHEDULE_INFO = "KEY_SCHEDULE_INFO"
        const val KEY_SELECTED_APP = "SELECTED_APP"
        const val KEY_SELECTED_APP_PACKAGE = "SELECTED_APP_PACKAGE"
        const val KEY_SELECTED_APP_CLASS = "SELECTED_APP_CLASS"
        const val KEY_MODIFY_SCHEDULE = "MODIFY_SCHEDULE"
        const val KEY_DELETE_SCHEDULE = "DELETE_SCHEDULE"
        const val KEY_ADD_SCHEDULE = "ADD_SCHEDULE"
        const val KEY_LAUNCH_TIME = "LAUNCH_TIME"
        const val KEY_NOTI_ID = "NOTIFICATION_ID"
        const val KEY_PHONE_APP_SAVE_STATUS = "PHONE_APP_SAVE_STATUS"
        const val KEY_BROADCAST_REQUEST_CODE = "KEY_BROADCAST_REQUEST_CODE"
        const val SHARED_PREF_APP_SCHEDULE = "com.khalid.appscheduler"
        const val KEY_USER_NOTI_CHECK = "USER_NOTI_CHECK"
//        const val STATUS_APP_LAUNCH_SCHEDULED = 0
//        const val STATUS_APP_LAUNCH_SUCCESS = 1
//        const val STATUS_APP_LAUNCH_FAILED = -1
//        const val STATUS_APP_LAUNCH_CANCELLED = -2
        const val INVALID_PRIMARY_KEY = -1
        const val INVALID_INDEX = -1
        const val DUPLICATE_LAUNCH_TIME = -1
        const val SEND_BROADCAST_REQUEST_LAUNCH_APP = 0
        const val DB_NO_ENTRY_FOUND = 0
        const val DB_CRUD_SUCCESS = 0

        enum class ShowNotification(val notiType: Int) {
            NOT_SHOWING(0),
            SHOWING(1),
            SHOWING_DONE(2)
        }

        enum class LaunchSchedule(val status: Int) {
            LAUNCH_SCHEDULED(0),
            LAUNCH_SUCCESS(1),
            LAUNCH_NOT_YET(2),
            LAUNCH_CANCELLED(3)
        }

        enum class InputType(val type: Int) {
            SELECT_APP(0),
            SELECT_TIME_DATE(1)
        }

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

        fun dismissLaunchNotification(context: Context, notiId: Int) {
            NotificationManagerCompat.from(context).cancel(notiId)
        }

        fun showLaunchNotification(
            context: Context?,
            notiId: Int,
            pendingIntent: PendingIntent
        ) {
            context?.let {
                val notification = createAppLaunchNotification(context, pendingIntent)
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.notify(notiId, notification)
                AppScheduleLog.d(TAG, "[showLaunchNotification]: $notiId is posted on status bar, updating db..")
            }
        }

        fun createNotificationChannel(context: Context) {
            // Create NotificationChannel.
            val name = context.resources.getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_NAME, name, importance)
            // Register the channel with the system.
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fun createAppLaunchNotification(
            context: Context,
            pendingIntent: PendingIntent
        ): Notification {
            val notification = NotificationCompat.Builder(context, CHANNEL_NAME)
                .setContentTitle(context.resources.getString(R.string.notification_name))
                .setContentText(context.resources.getString(R.string.notification_description))
                .setSmallIcon(R.mipmap.ic_schedule_app_launch)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setTimeoutAfter(60000)
                .build()
            return notification
        }

        fun isAppNotFound(context: Context?, packageName: String?): Boolean {
            val isInstalled = context?.packageManager?.getLaunchIntentForPackage(packageName ?: "") != null
            return isInstalled
        }

        fun getPhoneAlarmManager(context: Context) : AlarmManager? {
            val alarmManager : AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            return alarmManager
        }

        fun setUserNotiCheckStatus(context: Context, status: Boolean) {
            val sharedPref = context.getSharedPreferences(
                AppSchedulerUtils.SHARED_PREF_APP_SCHEDULE,
                MODE_PRIVATE
            )
            sharedPref.edit {
                apply {
                    putBoolean(AppSchedulerUtils.KEY_USER_NOTI_CHECK, status)
                }
            }
        }

        fun schedule(
            context: Context,
            schedule: AppLaunchSchedule?,
            pendingIntent: PendingIntent,
            launchTime: Long? = null
        ) {
            var scheduleAt = schedule?.launchTime?.time?.toLong() ?: 0L
            launchTime?.let {
                scheduleAt = launchTime
            }
            val alarmManager = getPhoneAlarmManager(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val isGrantedPermission = alarmManager?.canScheduleExactAlarms() == true
                if (isGrantedPermission == true) {
                    AppScheduleLog.d(
                        TAG,
                        "[schedule]: ${schedule?.packageName} launch is scheduled successfully by setExact api"
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduleAt,
                        pendingIntent
                    )
                } else {
                    AppScheduleLog.d(
                        TAG,
                        "[schedule]: ${schedule?.packageName} launch permission is not granted by system"
                    )
                    alarmManager?.set(
                        AlarmManager.RTC_WAKEUP,
                        scheduleAt,
                        pendingIntent
                    )
                }
            } else {
                AppScheduleLog.d(
                    TAG,
                    "[schedule]: ${schedule?.packageName} launch is scheduled successfully by setExactAndAllowWhileIdle api"
                )
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduleAt,
                    pendingIntent
                )
            }
        }
    }
}