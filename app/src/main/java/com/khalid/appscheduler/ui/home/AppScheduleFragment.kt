package com.khalid.appscheduler.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.common.viewmodel.ScheduleAppViewModel
import com.khalid.appscheduler.databinding.FragmentAppScheduleBinding
import com.khalid.appscheduler.listener.AppScheduleUpdateListener
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.ui.modifySchedule.ModifyScheduleActivity
import com.khalid.appscheduler.utils.AppSchedulerUtils
import kotlinx.coroutines.launch

class AppScheduleFragment : Fragment(), AppScheduleUpdateListener {

    private var TAG = "AppScheduleFragment"
    private var _binding: FragmentAppScheduleBinding? = null
    private var launcher : ActivityResultLauncher<Intent>? = null
    private val binding get() = _binding!!
    private var viewModel : ScheduleAppViewModel? = null
    private var recyclerView: RecyclerView? = null
    private var scheduleAppCount = 0
    private var appScheduleAdapter : AppScheduleAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppScheduleBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initViews()
        setupViewModel()
        setObserver()
        registerLauncher()
        return root
    }

    fun initViews() {
        recyclerView = binding.upcomingScheduleList
        appScheduleAdapter = AppScheduleAdapter(mutableListOf(), this)
        recyclerView?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = appScheduleAdapter
        }
    }

    private fun registerLauncher() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            AppScheduleLog.d(TAG, "[registerLauncher] result code: ${it.resultCode}, result data: ${it.data}")
            if (it.resultCode == AppSchedulerUtils.RESULT_SUCCESS) {
                AppScheduleLog.d(TAG,"[registerLauncher] current app schedule is updated.")
//                updateAppScheduleList()
            } else {
                AppScheduleLog.d(TAG,"[registerLauncher] No app schedule is updated.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppScheduleLog.d(TAG, "[onResume] schedule app count: $scheduleAppCount")
    }
    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )[ScheduleAppViewModel::class.java]

        viewModel?.deleteSuccessStatus?.observe(viewLifecycleOwner, Observer {
            if(it) {
                AppScheduleLog.d(TAG, "[setupViewModel] delete success")
            } else {
                AppScheduleLog.d(TAG, "[setupViewModel] delete failure")
                Toast.makeText(context, "Delete failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel?.scheduleAppState?.collect { scheduledAppList ->
                    AppScheduleLog.d(TAG, "[setObserver] UI resumes, schedule app list: ${scheduledAppList.size}")
                    scheduleAppCount = scheduledAppList.size
                    if (scheduleAppCount == 0) {
                        binding.scheduleInfoCardview.visibility = View.GONE
                        binding.noItemView.visibility = View.VISIBLE
                    } else {
                        binding.scheduleInfoCardview.visibility = View.VISIBLE
                        binding.noItemView.visibility = View.GONE
                        appScheduleAdapter?.scheduleList =
                            scheduledAppList as MutableList<AppLaunchSchedule>
                        appScheduleAdapter?.notifyDataSetChanged() // TODO: Notify only the changed item
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun launchModifyAppSchedule(schedule: AppLaunchSchedule) {
        val intent = Intent(context, ModifyScheduleActivity::class.java)
        intent.putExtra(AppSchedulerUtils.KEY_MODIFY_SCHEDULE, schedule)
        launcher?.launch(intent)
    }

    private fun deleteAppSchedule(schedule: AppLaunchSchedule) {
        lifecycleScope.launch {
            viewModel?.cancelAppLaunch(schedule) // cancel schedule
            viewModel?.deleteSchedule(schedule) // delete entry from database
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