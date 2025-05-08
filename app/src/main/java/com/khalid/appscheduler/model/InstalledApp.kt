package com.khalid.appscheduler.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val className: String,
    val appTitle: String,
    val icon: Drawable
)
