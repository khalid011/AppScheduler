package com.khalid.appscheduler.listener

import com.khalid.appscheduler.model.Schedule
import com.khalid.appscheduler.repository.model.AppLaunchSchedule

interface AppScheduleUpdateListener {
    fun onUpdate(schedule : AppLaunchSchedule, updateType: String)
}