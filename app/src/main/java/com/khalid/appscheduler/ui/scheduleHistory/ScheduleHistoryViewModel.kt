package com.khalid.appscheduler.ui.scheduleHistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScheduleHistoryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "No successful app schedule is found."
    }
    val text: LiveData<String> = _text
}