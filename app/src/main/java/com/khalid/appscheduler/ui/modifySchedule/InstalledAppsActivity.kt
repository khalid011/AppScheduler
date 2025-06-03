package com.khalid.appscheduler.ui.modifySchedule

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.R
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.databinding.ActivityInstalledAppsBinding
import com.khalid.appscheduler.listener.AppSelectionListener
import com.khalid.appscheduler.repository.model.InstalledAppInfo
import com.khalid.appscheduler.utils.AppSchedulerUtils
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
    private var isPhoneAppsSaved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstalledAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        getPhoneAppsSaveStatus()
        initObservers()
        setupListeners()
    }

    private fun getPhoneAppsSaveStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPref = getSharedPreferences(
                AppSchedulerUtils.SHARED_PREF_APP_SCHEDULE,
                MODE_PRIVATE
            )
            isPhoneAppsSaved =
                sharedPref.getBoolean(AppSchedulerUtils.KEY_PHONE_APP_SAVE_STATUS, false)
            AppScheduleLog.d(TAG,"isPhoneAppsSaved: $isPhoneAppsSaved")
            if (!isPhoneAppsSaved) {
                viewModel?.getInstalledAppsFromSystem()
                sharedPref.edit {
                    putBoolean(
                        AppSchedulerUtils.KEY_PHONE_APP_SAVE_STATUS,
                        true
                    )
                } // saving phone apps save db status
            } else {
                viewModel?.getInstalledPhoneAppsFromDB()
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[InstalledAppsViewModel::class.java]
    }

    private fun initObservers() {
        viewModel?.isLoadingProgress?.observe(this, Observer { isLoading ->
            AppScheduleLog.d(
                TAG,
                "[initViews] received progress bar status, isLoading: $isLoading, time: ${System.currentTimeMillis()}"
            )
            if (isLoading) {
                binding.installedAppinfoTitle.text =
                    this.getString(R.string.loading_progress_bar_text)
                binding.installedAppinfoCardview.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.installedAppinfoTitle.text = this.getString(R.string.installed_app_title)
                binding.progressBar.visibility = View.GONE
            }
        })
        viewModel?.installedApps?.observe(this, Observer { installedApps ->
            AppScheduleLog.d(
                TAG,
                "[initViews] received installed apps, size: ${installedApps.size}, time: ${System.currentTimeMillis()}"
            )
            binding.installedAppinfoTitle.text = this.getString(R.string.installed_app_title)
            binding.installedAppinfoCardview.visibility = View.VISIBLE
            initRecyclerView(installedApps)
//            binding.progressBar.visibility = View.GONE
//            binding.installedAppinfoCardview.visibility = View.VISIBLE
        })
    }

    private fun initRecyclerView(installedApps: List<InstalledAppInfo>?) {
        val recyclerView: RecyclerView = binding.installedAppinfoRecyclerview
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InstalledAppsActivity)
            adapter = InstalledAppsAdapter(
                installedApps ?: listOf(),
                this@InstalledAppsActivity
            )
            addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun setupListeners() {
        binding.cancelSelectedApp.setOnClickListener {
            AppScheduleLog.d(TAG, "[initViews] cancel app selection")
            val intent = Intent()
            setResult(AppSchedulerUtils.RESULT_CANCELED, intent)
            finish()
        }
        binding.doneSelectedApp.setOnClickListener {
            AppScheduleLog.d(TAG, "[initViews] done app selection")
            if (selectedAppTitle.isEmpty()) {
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

    override fun onConfirmAppSelection(packageName: String, className: String, appTitle: String) {
        AppScheduleLog.d(
            TAG,
            "[onConfirmAppSelection] packageName: $packageName, className: $className, appTitle: $appTitle"
        )
        selectedAppPackageName = packageName
        selectedAppClassname = className
        selectedAppTitle = appTitle
    }

}