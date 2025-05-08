package com.khalid.appscheduler.ui.modifySchedule

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.khalid.appscheduler.R
import com.khalid.appscheduler.databinding.ActivityModifyScheduleBinding
import com.khalid.appscheduler.model.Schedule
import com.khalid.appscheduler.ui.utils.AppSchedulerUtils
import java.util.Date
import kotlin.jvm.java

class ModifyScheduleActivity : AppCompatActivity() {

    private val TAG = "ModifyScheduleActivity"
    private lateinit var binding: ActivityModifyScheduleBinding
    private var launcher : ActivityResultLauncher<Intent>? = null

    private var selectedAppPackageName: String? = null
    private var selectedAppClassName: String? = null
    private var selectedAppLaunchTime: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setupListeners()
        registerLauncher()
    }

    private fun registerLauncher() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.d(TAG, "[registerLauncher] result code: ${it.resultCode}")
            Log.d(TAG, "[registerLauncher] result data: ${it.data}")
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
            Log.d(TAG, "[setupListeners] new scheduled app name clicked...Launch installed apps")
            launcher?.launch(Intent(this, InstalledAppsActivity::class.java))
        }
        binding.newScheduledLaunchTime.setOnClickListener {
            Log.d(TAG, "[setupListeners] new scheduled launch time clicked...Launch date/time picker")
            SingleDateAndTimePickerDialog.Builder(this)
                .bottomSheet()
                .title(getString(R.string.select_date_and_time))
                .listener {
                    if(it != null) {
                        binding.newScheduledLaunchTime.text = it.toString()
                        selectedAppLaunchTime = it
                        Log.d(TAG, "[setupListeners] selected launch time: $selectedAppLaunchTime")
                    }
                }
                .display()

        }
        binding.cancelSelectedApp.setOnClickListener {
            Log.d(TAG, "[setupListeners] cancel button clicked...")
            finish()
        }
        binding.doneSelectedApp.setOnClickListener {
            Log.d(TAG, "[setupListeners] cancel button clicked...")
            if(selectedAppPackageName != null && selectedAppClassName != null && selectedAppLaunchTime != null) {
                Log.d(TAG, "[setupListeners] save app launch schedule into db:\n" +
                        "packageName: $selectedAppPackageName\n" +
                        "className: $selectedAppClassName\n" +
                        "launchTime: $selectedAppLaunchTime"
                )
                finish()
            } else {
                Toast.makeText(this, "Please select app and launch time", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        if(intent.hasExtra("schedule")) {
            binding.currentScheduleCardview.visibility = View.VISIBLE
            val schedule = intent.getParcelableExtra<Schedule>("schedule") as? Schedule
            binding.currentScheduledAppName.text = schedule?.appName
            binding.currentScheduledLaunchTime.text = schedule?.launchTime
        } else {
            binding.currentScheduleCardview.visibility = View.GONE
        }

    }
}