package com.khalid.appscheduler.ui.scheduleHistory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.common.viewmodel.ScheduleAppViewModel
import com.khalid.appscheduler.databinding.FragmentScheduleHistoryBinding
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import kotlinx.coroutines.launch

class ScheduleHistoryFragment : Fragment() {

    private var TAG = "ScheduleHistoryFragment"
    private var _binding: FragmentScheduleHistoryBinding? = null
    private val binding get() = _binding!!
    private var launcher : ActivityResultLauncher<Intent>? = null
    private var viewModel : ScheduleAppViewModel? = null
    private var recyclerView: RecyclerView? = null
    private var scheduleAppCount = 0
    private var appScheduleAdapter : AppScheduleHistoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initViews()
        setupViewModel()
        setObserver()
        return root
    }


    fun initViews() {
        recyclerView = binding.successfulScheduleList
        appScheduleAdapter = AppScheduleHistoryAdapter(mutableListOf())
        recyclerView?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = appScheduleAdapter
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
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel?.previousScheduleAppState?.collect { scheduledAppList ->
                    AppScheduleLog.d(
                        TAG,
                        "[setObserver] UI resumes, successful scheduled app list: ${scheduledAppList.size}"
                    )
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
}