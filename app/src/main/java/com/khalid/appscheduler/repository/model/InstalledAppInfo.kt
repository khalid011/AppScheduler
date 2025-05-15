package com.khalid.appscheduler.repository.model

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_app_info")
data class InstalledAppInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val className: String,
)
