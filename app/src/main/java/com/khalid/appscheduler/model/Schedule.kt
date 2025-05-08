package com.khalid.appscheduler.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Schedule(
    val appName: String? = null,
    val launchTime: String? = null
) : Parcelable
