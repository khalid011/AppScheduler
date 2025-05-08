package com.khalid.appscheduler.ui.scheduleHistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScheduleHistoryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is schedule history Fragment"
    }
    val text: LiveData<String> = _text
}