package com.khalid.appscheduler.ui.modifySchedule

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.khalid.appscheduler.R
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.common.viewmodel.ScheduleAppViewModel
import com.khalid.appscheduler.databinding.ActivityModifyScheduleBinding
import com.khalid.appscheduler.model.Schedule
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.utils.AppSchedulerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date

class ModifyScheduleActivity : AppCompatActivity() {

    private val TAG = "ModifyScheduleActivity"
    private lateinit var binding: ActivityModifyScheduleBinding
    private var launcher : ActivityResultLauncher<Intent>? = null
    private var selectedAppPackageName: String? = null
    private var selectedAppClassName: String? = null
    private var selectedAppLaunchTime: Date? = null
    private var currentSchedule : AppLaunchSchedule? = null
    private var viewModel: ScheduleAppViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        initViews()
        setupListeners()
        registerLauncher()
    }


    private fun initViews() {
        if(intent.hasExtra(AppSchedulerUtils.KEY_MODIFY_SCHEDULE)) {
            binding.currentScheduleCardview.visibility = View.VISIBLE
            currentSchedule = intent.getParcelableExtra<Schedule>(AppSchedulerUtils.KEY_MODIFY_SCHEDULE) as AppLaunchSchedule
            binding.currentScheduledAppName.text = AppSchedulerUtils.getAppTitle(
                this@ModifyScheduleActivity,
                currentSchedule?.packageName ?: "",
                currentSchedule?.className ?: ""
            )
            binding.currentScheduledLaunchTime.text = AppSchedulerUtils.getLaunchTime(currentSchedule?.launchTime)
        } else {
            // Add new schedule, no current schedule available on that timestamp
            binding.currentScheduleCardview.visibility = View.GONE
        }

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[ScheduleAppViewModel::class.java]
    }

    private fun registerLauncher() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            AppScheduleLog.d(TAG, "[registerLauncher] result code: ${it.resultCode}")
            AppScheduleLog.d(TAG, "[registerLauncher] result data: ${it.data}")
            if (it.resultCode == AppSchedulerUtils.RESULT_SUCCESS) {
                binding.newScheduledAppName.text =
                    it.data?.getStringExtra(AppSchedulerUtils.KEY_SELECTED_APP)
                selectedAppPackageName = it.data?.getStringExtra(AppSchedulerUtils.KEY_SELECTED_APP_PACKAGE)
                selectedAppClassName = it.data?.getStringExtra(AppSchedulerUtils.KEY_SELECTED_APP_CLASS)
            } else {
                binding.newScheduledAppName.text = getString(R.string.title_modify_schedule)
            }
        }
    }

    private fun setupListeners() {
        binding.newScheduledAppName.setOnClickListener {
            AppScheduleLog.d(TAG, "[setupListeners] new scheduled app name clicked...Launch installed apps")
            launcher?.launch(Intent(this, InstalledAppsActivity::class.java))
        }
        binding.newScheduledLaunchTime.setOnClickListener {
            AppScheduleLog.d(TAG, "[setupListeners] new scheduled launch time clicked...Launch date/time picker")
            launchDateAndTimePicker()
        }
        binding.cancelSelectedApp.setOnClickListener {
            AppScheduleLog.d(TAG, "[setupListeners] updating/adding schedule 'Cancel' button is clicked...")
            finish()
        }
        binding.doneSelectedApp.setOnClickListener {
            AppScheduleLog.d(TAG, "[setupListeners] updating/adding schedule 'Done' button is clicked...")
            handleDoneButton()
        }
    }

    private fun launchDateAndTimePicker() {
        val picker = SingleDateAndTimePickerDialog.Builder(this)
            .bottomSheet()
            .title(getString(R.string.select_date_and_time))
            .minutesStep(1)
            .minDateRange(Date(System.currentTimeMillis()))
            .listener {
                if(it != null) {
                    binding.newScheduledLaunchTime.text = it.toString()
                    selectedAppLaunchTime = it
                    AppScheduleLog.d(TAG, "[setupListeners] selected launch time: $selectedAppLaunchTime")
                }
            }
        picker.display()
    }

    private fun processSchedule(result: Int) : Boolean {
        AppScheduleLog.d(TAG, "[processSchedule] result: $result")
        if(result != AppSchedulerUtils.DUPLICATE_LAUNCH_TIME) {
            scheduleAppLaunch()
            return true
        } else {
            Toast.makeText(this, getString(R.string.title_toast_show_duplicate_entry_error), Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun handleDoneButton() {
        if(selectedAppPackageName != null && selectedAppClassName != null && selectedAppLaunchTime != null) {
            AppScheduleLog.d(TAG, "[handleDoneButton] handling 'Done' button, launch schedule for:\n" +
                    "packageName: $selectedAppPackageName\n" +
                    "className: $selectedAppClassName\n" +
                    "launchTime: $selectedAppLaunchTime"
            )
            if(intent.hasExtra(AppSchedulerUtils.KEY_MODIFY_SCHEDULE)) {
                updateAppSchedule()?.let { result ->
                    val isScheduleUpdated = processSchedule(result)
                    if(isScheduleUpdated) {
                        // Cancel previous schedule
                        cancelSchedule()
                        sendSuccessResult()
                    }

               }
            } else if(intent.hasExtra(AppSchedulerUtils.KEY_ADD_SCHEDULE)) {
                insertAppSchedule()?.let { result ->
                    val isScheduleUpdated = processSchedule(result)
                    if(isScheduleUpdated) {
                        sendSuccessResult()
                    }
                }
            } else {
                AppScheduleLog.d(TAG,"[handleDoneButton] Unknown intent received")
            }
        } else {
            Toast.makeText(this, getString(R.string.title_toast_show_error_message), Toast.LENGTH_SHORT).show()
        }

    }

    private fun cancelSchedule() {
        viewModel?.cancelAppLaunch(
            currentSchedule?.packageName ?: "",
            currentSchedule?.className ?: "",
            currentSchedule?.launchTime?.time ?: 0
        )
    }

    private fun scheduleAppLaunch() {
        viewModel?.scheduleAppLaunch(selectedAppPackageName!!, selectedAppClassName!!, selectedAppLaunchTime!!.time)
    }


    private fun sendSuccessResult() {
        val intent = Intent()
        setResult(AppSchedulerUtils.RESULT_SUCCESS, intent)
        finish()
    }

    private fun isSelectedAppScheduleValid() : Boolean {
        // Check if the selected date and time is found in existing schedule (query in db)
        selectedAppLaunchTime?.let { launchTime ->
            if(viewModel?.getScheduleByLaunchTime(launchTime) == 0) {
                AppScheduleLog.d(TAG, "[isSelectedAppScheduleValid] No schedule found in db")
                return true
            }
        }
        AppScheduleLog.d(TAG, "[isSelectedAppScheduleValid] previous schedule found in db")
        return false
    }

    private fun updateAppSchedule() : Int? {
        AppScheduleLog.d(TAG, "[updateAppSchedule] updating app schedule...")
        var updateResult : Int? = AppSchedulerUtils.DUPLICATE_LAUNCH_TIME
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                currentSchedule?.let { schedule ->
                    updateResult = viewModel?.updateSchedule(
                        AppLaunchSchedule(
                            id = schedule.id,
                            packageName = selectedAppPackageName!!,
                            className = selectedAppClassName!!,
                            launchTime = selectedAppLaunchTime!!,
                            launchStatus = AppSchedulerUtils.STATUS_APP_LAUNCH_SCHEDULED
                        )
                    )
                }
            }.join()
        }
        return updateResult
    }

    private fun insertAppSchedule() : Int? {
        AppScheduleLog.d(TAG, "[insertAppSchedule] inserting app schedule...")
        var insertionResult : Int? = AppSchedulerUtils.DUPLICATE_LAUNCH_TIME
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                insertionResult = viewModel?.insertSchedule(
                    AppLaunchSchedule(
                        packageName = selectedAppPackageName!!,
                        className = selectedAppClassName!!,
                        launchTime = selectedAppLaunchTime!!,
                        launchStatus = AppSchedulerUtils.STATUS_APP_LAUNCH_SCHEDULED
                    )
                )
            }.join()
        }
        return insertionResult
    }
}