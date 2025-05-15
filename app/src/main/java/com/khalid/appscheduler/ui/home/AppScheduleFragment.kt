package com.khalid.appscheduler.ui.home

import android.R.attr.visibility
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.common.viewmodel.ScheduleAppViewModel
import com.khalid.appscheduler.databinding.FragmentAppScheduleBinding
import com.khalid.appscheduler.listener.AppScheduleUpdateListener
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.ui.modifySchedule.ModifyScheduleActivity
import com.khalid.appscheduler.utils.AppSchedulerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppScheduleFragment : Fragment(), AppScheduleUpdateListener {

    private var TAG = "HomeFragment"
    private var _binding: FragmentAppScheduleBinding? = null
    private var launcher : ActivityResultLauncher<Intent>? = null
    private val binding get() = _binding!!
    private var viewModel : ScheduleAppViewModel? = null
    private var recyclerView: RecyclerView? = null
    private var appScheduleAdapter : AppScheduleAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppScheduleBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setupViewModel()
        initViews()
        registerLauncher()
        return root
    }

    private fun registerLauncher() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            AppScheduleLog.d(TAG, "[registerLauncher] result code: ${it.resultCode}, result data: ${it.data}")
            if (it.resultCode == AppSchedulerUtils.RESULT_SUCCESS) {
                AppScheduleLog.d(TAG,"[registerLauncher] current app schedule is updated.")
                updateAppScheduleList()
            } else {
                AppScheduleLog.d(TAG,"[registerLauncher] No app schedule is updated.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAppScheduleList()
    }
    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )[ScheduleAppViewModel::class.java]

        viewModel?.deleteSuccessStatus?.observe(viewLifecycleOwner, Observer {
            if(it) {
                AppScheduleLog.d(TAG, "[setupViewModel] delete success")
                updateAppScheduleList()
                // TODO: Cancel the schedule
            } else {
                AppScheduleLog.d(TAG, "[setupViewModel] delete failure")
                Toast.makeText(context, "Delete failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initViews() {
        lifecycleScope.launch(Dispatchers.Default) {
            val listOfSchedule = viewModel?.getAllSchedules()?.toMutableList() ?: emptyList()
            recyclerView = binding.upcomingScheduleList
            appScheduleAdapter = AppScheduleAdapter(listOfSchedule as MutableList<AppLaunchSchedule>, this@AppScheduleFragment)
            recyclerView?.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = appScheduleAdapter
//                visibility = View.GONE
            }
            withContext(Dispatchers.Main) {
                updateView(listOfSchedule.size)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateView(scheduleCount : Int) {
        AppScheduleLog.d(TAG, "[updateView] schedule count: $scheduleCount")
        if(scheduleCount == 0) {
            binding.scheduleInfoCardview.visibility = View.GONE
            binding.noItemView.visibility = View.VISIBLE
            binding.noItemView.bringToFront()
        } else if(scheduleCount > 0) {
            binding.noItemView.visibility = View.GONE
            binding.scheduleInfoCardview.visibility = View.VISIBLE
//            binding.scheduleInfoCardview.requestLayout()
        } else {
            binding.scheduleInfoCardview.visibility = View.GONE
        }
    }

    private fun updateAppScheduleList() {
        lifecycleScope.launch(Dispatchers.Default) {
            val updatedScheduleList = viewModel?.getAllSchedules()?.toMutableList() ?: emptyList()
            appScheduleAdapter?.scheduleList = updatedScheduleList as MutableList<AppLaunchSchedule>
            withContext(Dispatchers.Main) {
                updateView(updatedScheduleList.size)
                appScheduleAdapter?.notifyDataSetChanged() // TODO: Notify only the changed item
            }
        }
    }

    private fun launchModifyAppSchedule(schedule: AppLaunchSchedule) {
        val intent = Intent(context, ModifyScheduleActivity::class.java)
        intent.putExtra(AppSchedulerUtils.KEY_MODIFY_SCHEDULE, schedule)
        launcher?.launch(intent)
    }

    private fun deleteAppSchedule(schedule: AppLaunchSchedule) {
        lifecycleScope.launch {
            viewModel?.cancelAppLaunch(schedule.packageName, schedule.className, schedule.launchTime?.time ?: 0)
            viewModel?.deleteSchedule(schedule)
        }
    }

    override fun onUpdate(schedule: AppLaunchSchedule, updateType: String) {
        if (updateType == AppSchedulerUtils.KEY_MODIFY_SCHEDULE) {
            launchModifyAppSchedule(schedule)
        } else if(updateType == AppSchedulerUtils.KEY_DELETE_SCHEDULE) {
            deleteAppSchedule(schedule)
        } else {
            AppScheduleLog.d(TAG,"[onUpdate] unknown update type received")
        }
    }
}