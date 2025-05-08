package com.khalid.appscheduler.ui.listener

import com.khalid.appscheduler.model.Schedule

interface AppScheduleUpdateListener {
    fun onUpdate(schedule : Schedule)
}