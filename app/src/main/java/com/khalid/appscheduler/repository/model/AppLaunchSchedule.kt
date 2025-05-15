package com.khalid.appscheduler.repository.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "app_launch_schedule", indices = [Index(value = ["launchTime"], unique = true)])
@Parcelize
data class AppLaunchSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val className: String,
    val launchTime: Date?,
    val launchStatus: Int // 0 -> not launched, 1 -> launched, -1 -> failed to launch
) : Parcelable