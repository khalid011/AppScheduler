package com.khalid.appscheduler.ui.home

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

class HomeFragment : Fragment() {

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

        val listOfSchedule = getSchedule()
        val recyclerView : RecyclerView = binding.upcomingScheduleList
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = AppScheduleAdapter(listOfSchedule)
        }
        return root
    }

    private fun getSchedule() : MutableList<Schedule> {
        val listOfSchedule = mutableListOf<Schedule>()
        listOfSchedule.apply {
            add(Schedule("9:00AM"))
            add(Schedule("10:00AM"))
            add(Schedule("11:00AM"))
            add(Schedule("12:00AM"))
            add(Schedule("9:00PM"))
            add(Schedule("10:00PM"))
            add(Schedule("11:00PM"))
            add(Schedule("12:00PM"))
        }
        return listOfSchedule
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}