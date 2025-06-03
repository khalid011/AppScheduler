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
import com.khalid.appscheduler.utils.AppSchedulerUtils.Companion.InputType
import com.khalid.appscheduler.utils.AppSchedulerUtils.Companion.schedule
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
            binding.newScheduleInfoTitle.text = "Updated schedule"
            binding.newScheduleAppNameLayout.visibility = View.GONE
            binding.newScheduleAppLaunchTime.text = "New time schedule"
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

    private fun processSchedule(result: Int, type: InputType) : Boolean {
        AppScheduleLog.d(TAG, "[processSchedule] result: $result, type: $type")
        if(result != AppSchedulerUtils.DUPLICATE_LAUNCH_TIME) {
            scheduleAppLaunch(type)
            return true
        } else {
            Toast.makeText(this, getString(R.string.title_toast_show_duplicate_entry_error), Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun isInputValid(type: InputType) : Boolean {
        AppScheduleLog.d(TAG, "[isInputValid] packageName: $selectedAppPackageName\n" +
                "className: $selectedAppClassName\n" +
                "launchTime: $selectedAppLaunchTime"
        )
        if (type == InputType.SELECT_APP) {
            return selectedAppPackageName != null && selectedAppClassName != null && selectedAppLaunchTime != null
        }
        return selectedAppLaunchTime != null
    }

    private fun handleDoneButton() {
        when {
            intent.hasExtra(AppSchedulerUtils.KEY_MODIFY_SCHEDULE) -> {
                if(isInputValid(InputType.SELECT_TIME_DATE)) {
                    updateAppSchedule()?.let { result ->
                        val isScheduleUpdated = processSchedule(result, InputType.SELECT_TIME_DATE)
                        if(isScheduleUpdated) {
                            // Cancel previous schedule
                            cancelSchedule()
                            sendSuccessResult()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.title_toast_show_error_message_modify_launch_time),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            intent.hasExtra(AppSchedulerUtils.KEY_ADD_SCHEDULE) -> {
                if(isInputValid(InputType.SELECT_APP)) {
                    insertAppSchedule()?.let { result ->
                        val isScheduleUpdated = processSchedule(result, InputType.SELECT_APP)
                        if(isScheduleUpdated) {
                            sendSuccessResult()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.title_toast_show_error_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                AppScheduleLog.d(TAG,"[handleDoneButton] Unknown intent received")
            }
        }

    }

    private fun cancelSchedule() {
        viewModel?.cancelAppLaunch(
            AppLaunchSchedule(
                id = currentSchedule?.id ?: 0,
                packageName = currentSchedule?.packageName ?: "",
                className = currentSchedule?.className ?: "",
                launchTime = currentSchedule?.launchTime ?: Date(),
                launchStatus = AppSchedulerUtils.Companion.LaunchSchedule.LAUNCH_CANCELLED.status,
                showNotification = AppSchedulerUtils.Companion.ShowNotification.NOT_SHOWING.notiType
            )
//            currentSchedule?.packageName ?: "",
//            currentSchedule?.className ?: "",
//            currentSchedule?.launchTime?.time ?: 0
        )
    }

    private fun scheduleAppLaunch(type: InputType) {
        if(type == InputType.SELECT_TIME_DATE) {
            currentSchedule?.let { schedule ->
                viewModel?.scheduleAppLaunch(
                    AppLaunchSchedule(
                        id = schedule.id,
                        packageName = schedule.packageName,
                        className = schedule.className,
                        launchTime = selectedAppLaunchTime!!,
                        launchStatus = AppSchedulerUtils.Companion.LaunchSchedule.LAUNCH_SCHEDULED.status,
                        showNotification = AppSchedulerUtils.Companion.ShowNotification.NOT_SHOWING.notiType
                    )
                )
            }
        } else {
            viewModel?.scheduleAppLaunch(
                AppLaunchSchedule(
                    packageName = selectedAppPackageName!!,
                    className = selectedAppClassName!!,
                    launchTime = selectedAppLaunchTime!!,
                    launchStatus = AppSchedulerUtils.Companion.LaunchSchedule.LAUNCH_SCHEDULED.status,
                    showNotification = AppSchedulerUtils.Companion.ShowNotification.NOT_SHOWING.notiType
                )
            )
        }
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
                            packageName = schedule.packageName,
                            className = schedule.className,
                            launchTime = selectedAppLaunchTime!!,
                            launchStatus = AppSchedulerUtils.Companion.LaunchSchedule.LAUNCH_SCHEDULED.status,
                            showNotification = AppSchedulerUtils.Companion.ShowNotification.NOT_SHOWING.notiType
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
                        launchStatus = AppSchedulerUtils.Companion.LaunchSchedule.LAUNCH_SCHEDULED.status,
                        showNotification = AppSchedulerUtils.Companion.ShowNotification.NOT_SHOWING.notiType
                    )
                )
            }.join()
        }
        return insertionResult
    }
}