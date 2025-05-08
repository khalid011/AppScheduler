package com.khalid.appscheduler.ui.listener

interface AppSelectionListener {
    fun onConfirmAppSelection(packageName: String, className: String, appName: String)
}