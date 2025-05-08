package com.khalid.appscheduler.ui.modifySchedule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Log.i
import android.util.TimeUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.R
import com.khalid.appscheduler.databinding.ActivityInstalledAppsBinding
import com.khalid.appscheduler.model.InstalledApp
import com.khalid.appscheduler.model.Schedule
import com.khalid.appscheduler.ui.home.AppScheduleAdapter
import com.khalid.appscheduler.ui.home.HomeFragment
import com.khalid.appscheduler.ui.listener.AppSelectionListener
import com.khalid.appscheduler.ui.utils.AppSchedulerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InstalledAppsActivity : AppCompatActivity(), AppSelectionListener {
    private val TAG = "InstalledAppsActivity"
    private lateinit var binding: ActivityInstalledAppsBinding
    private lateinit var selectedAppClassname: String
    private lateinit var selectedAppPackageName: String
    private var selectedAppTitle: String = ""
    private var viewModel: InstalledAppsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstalledAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        initViews()
        setupListeners()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(InstalledAppsViewModel::class.java)
        viewModel?.getInstalledApps()
    }

    private fun initViews() {
        val recyclerView: RecyclerView = binding.installedAppinfoRecyclerview
        viewModel?.isLoadingProgress?.observe(this, Observer { isLoading ->
            Log.d(TAG, "[initViews] received progress bar status, isLoading: $isLoading, time: ${System.currentTimeMillis()}")
            if(isLoading) {
                binding.installedAppinfoTitle.text = this.getString(R.string.loading_progress_bar_text)
                binding.installedAppinfoCardview.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.installedAppinfoTitle.text = this.getString(R.string.installed_app_title)
                binding.progressBar.visibility = View.GONE
            }
        })
        viewModel?.installedApps?.observe(this, Observer { installedApps ->
            Log.d(TAG, "[initViews] received installed apps, size: ${installedApps.size}, time: ${System.currentTimeMillis()}")
            binding.installedAppinfoTitle.text = this.getString(R.string.installed_app_title)
            binding.progressBar.visibility = View.GONE
            binding.installedAppinfoCardview.visibility = View.VISIBLE
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@InstalledAppsActivity)
                adapter = InstalledAppsAdapter(
                    installedApps ?: listOf(),
                    this@InstalledAppsActivity
                )
            }
        } )
    }

    private fun setupListeners() {
        binding.cancelSelectedApp.setOnClickListener {
            Log.d(TAG,"[initViews] cancel app selection")
            val intent = Intent()
            setResult(AppSchedulerUtils.RESULT_CANCELED, intent)
            finish()
        }
        binding.doneSelectedApp.setOnClickListener {
            Log.d(TAG,"[initViews] done app selection")
            if(selectedAppTitle.isEmpty()) {
                Toast.makeText(this, "Please select an app", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent()
                intent.apply {
                    putExtra(AppSchedulerUtils.KEY_SELECTED_APP, selectedAppTitle)
                    putExtra(AppSchedulerUtils.KEY_SELECTED_APP_PACKAGE, selectedAppPackageName)
                    putExtra(AppSchedulerUtils.KEY_SELECTED_APP_CLASS, selectedAppClassname)
                }
                setResult(AppSchedulerUtils.RESULT_SUCCESS, intent)
                finish()
            }
        }
    }

//    private fun getInstalledApps(): MutableList<InstalledApp> {
//
//        val listOfInstalledApp = mutableListOf<InstalledApp>()
//        // Get list of installed launchable app info from system
//        val intent = Intent(Intent.ACTION_MAIN,null).apply {
//            addCategory(Intent.CATEGORY_LAUNCHER)
//        }
//        val launchableApps = this.packageManager.queryIntentActivities(intent, 0)
//        Log.d(TAG, "[getInstalledApps] launchableApps count: ${launchableApps.size}")
//        for (app in launchableApps) {
//            Log.d(TAG,
//                "[getInstalledApps] packageName:${app.activityInfo.packageName},\n" +
//                        "className: ${app.activityInfo.name},\n" +
//                        "label: ${app.loadLabel(this.packageManager)}\n" +
//                        "drawable: ${app.loadIcon(this.packageManager)}\n"
//            )
//            if(app.activityInfo.packageName.equals(this.packageName)) {
//                Log.d(TAG,"[getInstalledApps] Excluding current app from list")
//            } else {
//                listOfInstalledApp.add(
//                    InstalledApp(
//                        app.activityInfo.packageName,
//                        app.activityInfo.name,
//                        app.loadLabel(this.packageManager).toString(),
//                        app.loadIcon(this.packageManager)
//                    )
//                )
//            }
//        }
//        return listOfInstalledApp.sortedBy { it.appTitle.lowercase() } as MutableList<InstalledApp>
//    }

    override fun onConfirmAppSelection(packageName: String, className: String, appTitle: String) {
        Log.d(TAG,"[onConfirmAppSelection] packageName: $packageName, className: $className, appTitle: $appTitle")
        selectedAppPackageName = packageName
        selectedAppClassname = className
        selectedAppTitle = appTitle
    }

}