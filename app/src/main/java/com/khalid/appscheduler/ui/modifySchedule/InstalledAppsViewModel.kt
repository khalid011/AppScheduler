package com.khalid.appscheduler.ui.modifySchedule

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.khalid.appscheduler.model.InstalledApp
import com.khalid.appscheduler.model.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.collections.mutableListOf

class InstalledAppsViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "InstalledAppsViewModel"

    private val _isLoadingProgress = MutableLiveData<Boolean>()
    val isLoadingProgress: LiveData<Boolean> = _isLoadingProgress

    private val _installedApps = MutableLiveData<List<InstalledApp>>()
    val installedApps: LiveData<List<InstalledApp>> = _installedApps

    fun getInstalledApps() {

        viewModelScope.launch {
            _isLoadingProgress.value = true
            val deferred = CoroutineScope(Dispatchers.Default).async {
                getInstalledAppsFromSystem()
            }
            _installedApps.value = deferred.await()
            _isLoadingProgress.value = false
        }
    }

    fun getInstalledAppsFromSystem(): MutableList<InstalledApp> {

        try {
            val listOfInstalledApp = mutableListOf<InstalledApp>()
            // Get list of installed launchable app info from system
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val pm = getApplication<Application>().packageManager
            val launchableApps = pm.queryIntentActivities(intent, 0)
            Log.d(TAG, "[getInstalledApps] launchableApps count: ${launchableApps.size}")
            for (app in launchableApps) {
                Log.d(
                    TAG,
                    "[getInstalledApps] packageName:${app.activityInfo.packageName},\n" +
                            "className: ${app.activityInfo.name},\n" +
                            "label: ${app.loadLabel(pm)}\n" +
                            "drawable: ${app.loadIcon(pm)}\n"
                )
                if (app.activityInfo.packageName.equals(getApplication<Application>().packageName)) {
                    Log.d(TAG, "[getInstalledApps] Excluding current app from list")
                } else {
                    listOfInstalledApp.add(
                        InstalledApp(
                            app.activityInfo.packageName,
                            app.activityInfo.name,
                            app.loadLabel(pm).toString(),
                            app.loadIcon(pm)
                        )
                    )
                }
            }
            return listOfInstalledApp.sortedBy { it.appTitle.lowercase() } as MutableList<InstalledApp>
        } catch (exception: Exception) {
            Log.d(TAG,"[getInstalledApps] exception: ${exception.stackTrace}")
        }
        return mutableListOf()
    }

}