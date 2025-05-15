package com.khalid.appscheduler.listener

interface AppSelectionListener {
    fun onConfirmAppSelection(packageName: String, className: String, appName: String)
}