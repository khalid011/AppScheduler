package com.khalid.appscheduler.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.R
import com.khalid.appscheduler.model.Schedule
import com.khalid.appscheduler.ui.listener.AppScheduleUpdateListener

class AppScheduleAdapter(
    private val scheduleList: List<Schedule>,
    private val listener: AppScheduleUpdateListener
) : RecyclerView.Adapter<AppScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppScheduleViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.schedule_info, parent, false)
        return AppScheduleViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: AppScheduleViewHolder, position: Int) {
        holder.bind(scheduleList.get(position))
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

}

class AppScheduleViewHolder(itemView: View, private val listener: AppScheduleUpdateListener) : RecyclerView.ViewHolder(itemView) {

    private var scheduleTime: TextView? = null
    private var scheduleCancel: Button? = null
    private var scheduleUpdate: Button? = null

    init {
        scheduleTime = itemView.findViewById(R.id.schedule_time)
        scheduleCancel = itemView.findViewById(R.id.schedule_cancel)
        scheduleUpdate = itemView.findViewById(R.id.schedule_edit)
    }

    fun bind(item: Schedule) {
        scheduleTime?.text = item.launchTime
        scheduleCancel?.setOnClickListener {

        }
        scheduleUpdate?.setOnClickListener {

            listener.onUpdate(item)
        }
    }
}
