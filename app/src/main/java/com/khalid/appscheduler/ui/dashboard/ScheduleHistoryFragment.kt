package com.khalid.appscheduler.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.khalid.appscheduler.databinding.FragmentScheduleHistoryBinding

class ScheduleHistoryFragment : Fragment() {

    private var _binding: FragmentScheduleHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scheduleHistoryViewModel =
            ViewModelProvider(this)[ScheduleHistoryDashboardViewModel::class.java]

        _binding = FragmentScheduleHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textScheduleHistory
        scheduleHistoryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}