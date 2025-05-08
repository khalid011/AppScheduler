package com.khalid.appscheduler.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.R
import com.khalid.appscheduler.model.Schedule

class AppScheduleAdapter(private val scheduleList: List<Schedule>) : RecyclerView.Adapter<AppScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_info, parent, false)
        return AppScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppScheduleViewHolder, position: Int) {
        holder.bind(scheduleList.get(position))
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

}

class AppScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var scheduleTime : TextView? = null

    init {
        scheduleTime = itemView.findViewById(R.id.schedule_time)
    }

    fun bind(item : Schedule) {
        scheduleTime?.text = item.scheduleTime
    }
}
