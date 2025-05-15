package com.khalid.appscheduler.ui.modifySchedule

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.repository.AppScheduleRepository
import com.khalid.appscheduler.repository.db.AppScheduleDB
import com.khalid.appscheduler.repository.model.InstalledAppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class InstalledAppsViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "InstalledAppsViewModel"
    private var repository: AppScheduleRepository? = null
    private val _isLoadingProgress = MutableLiveData<Boolean>()
    val isLoadingProgress: LiveData<Boolean> = _isLoadingProgress
    private val _installedApps = MutableLiveData<List<InstalledAppInfo>>()
    val installedApps: LiveData<List<InstalledAppInfo>> = _installedApps

    init {
        val db = AppScheduleDB.Companion.getInstance(getApplication())
        val dao = db.appScheduleDao()
        repository = AppScheduleRepository(dao)
    }

    fun getInstalledPhoneAppsFromDB() {
        var installedApps: List<InstalledAppInfo> = listOf()
        viewModelScope.launch(Dispatchers.IO) {
            installedApps = repository?.getAllInstalledAppInfo() ?: listOf()
        }
        AppScheduleLog.d(TAG, "[getInstalledPhoneAppsFromDB] installedApps count: ${installedApps.size}")
        viewModelScope.launch {
            _installedApps.value = installedApps
        }
    }

    fun getInstalledAppsFromSystem() {
        // get installed apps from package manager
        viewModelScope.launch {
            _isLoadingProgress.value = true
            val deferred = CoroutineScope(Dispatchers.Default).async {
                getAppsFromPackageManager()
            }
            _installedApps.value = deferred.await().apply {
                _isLoadingProgress.value = false
            }
        }
    }

    fun getAppsFromPackageManager(): MutableList<InstalledAppInfo> {

        try {
            val listOfInstalledApp = mutableListOf<InstalledAppInfo>()
            // Get list of installed launchable app info from system
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val pm = getApplication<Application>().packageManager
            val launchableApps = pm.queryIntentActivities(intent, 0)
            AppScheduleLog.d(TAG, "[getInstalledApps] launchableApps count: ${launchableApps.size}")
            for (app in launchableApps) {
                AppScheduleLog.d(
                    TAG,
                    "[getInstalledApps] packageName:${app.activityInfo.packageName},\n" +
                            "className: ${app.activityInfo.name},\n" +
                            "label: ${app.loadLabel(pm)}\n"
                )
                if (app.activityInfo.packageName.equals(getApplication<Application>().packageName)) {
                    AppScheduleLog.d(TAG, "[getInstalledApps] Excluding current app from list")
                } else {
                        val installedApp = InstalledAppInfo(
                            packageName = app.activityInfo.packageName,
                            className = app.activityInfo.name
                        )
                        viewModelScope.launch {
                            repository?.insertInstalledAppInfo(installedApp) // saving to db
                        }
                        listOfInstalledApp.add(installedApp)
                }
            }
            return listOfInstalledApp
        } catch (exception: Exception) {
            AppScheduleLog.d(TAG,"[getInstalledApps] exception: ${exception.stackTrace}")
        }
        return mutableListOf()
    }

}