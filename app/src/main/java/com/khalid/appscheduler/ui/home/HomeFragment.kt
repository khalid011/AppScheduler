package com.khalid.appscheduler.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.databinding.FragmentHomeBinding
import com.khalid.appscheduler.model.Schedule
import com.khalid.appscheduler.ui.listener.AppScheduleUpdateListener
import com.khalid.appscheduler.ui.modifySchedule.ModifyScheduleActivity

class HomeFragment : Fragment(), AppScheduleUpdateListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initViews()
        return root
    }

    private fun initViews() {
        val listOfSchedule = getSchedule()
        val recyclerView : RecyclerView = binding.upcomingScheduleList
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = AppScheduleAdapter(listOfSchedule, this@HomeFragment)
        }
    }

    private fun getSchedule() : MutableList<Schedule> {
        val listOfSchedule = mutableListOf<Schedule>()
        listOfSchedule.apply {
            add(Schedule(launchTime = "9:00AM"))
            add(Schedule(launchTime = "10:00AM"))
            add(Schedule(launchTime = "11:00AM"))
            add(Schedule(launchTime = "12:00AM"))
            add(Schedule(launchTime = "9:00PM"))
            add(Schedule(launchTime = "10:00PM"))
            add(Schedule(launchTime = "11:00PM"))
            add(Schedule(launchTime = "12:00PM"))
        }
        return listOfSchedule
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onUpdate(schedule: Schedule) {
        val intent = Intent(context, ModifyScheduleActivity::class.java)
        intent.putExtra("schedule", schedule)
        context?.startActivity(intent)
    }
}