package com.khalid.appscheduler.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScheduleHistoryDashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is schedule history Fragment"
    }
    val text: LiveData<String> = _text
}