package com.khalid.appscheduler.common.logger

import android.R.id.message
import android.util.Log

class AppScheduleLog {

    companion object {

        private val MAIN_TAG = "[AppScheduleLog]_"

        fun d(tag: String, message: String) {
            Log.d(MAIN_TAG, "$tag:_ $message")
        }
    }
}