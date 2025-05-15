package com.khalid.appscheduler.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.R
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.listener.AppScheduleUpdateListener
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.utils.AppSchedulerUtils

class AppScheduleAdapter(
    var scheduleList: MutableList<AppLaunchSchedule>,
    private val listener: AppScheduleUpdateListener
) : RecyclerView.Adapter<AppScheduleViewHolder>() {

    private val TAG = "AppScheduleAdapter"
    var itemSelected = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppScheduleViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.schedule_info, parent, false)
        return AppScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppScheduleViewHolder, position: Int) {
        holder.scheduleCancel?.setOnClickListener {
            AppScheduleLog.d(TAG,"schedule cancel button is clicked")
            listener.onUpdate(scheduleList[position], AppSchedulerUtils.KEY_DELETE_SCHEDULE)
        }
        holder.scheduleUpdate?.setOnClickListener {
            AppScheduleLog.d(TAG,"schedule update button is clicked")
            listener.onUpdate(scheduleList[position], AppSchedulerUtils.KEY_MODIFY_SCHEDULE)
        }
        holder.bind(scheduleList[position])
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }
}

class AppScheduleViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    var scheduleAppIcon: ImageView? = null
    var scheduleAppTitle: TextView? = null
    var scheduleTime: TextView? = null
    var scheduleCancel: Button? = null
    var scheduleUpdate: Button? = null

    init {
        scheduleAppIcon = itemView.findViewById(R.id.scheduled_app_icon)
        scheduleAppTitle = itemView.findViewById(R.id.scheduled_app_title)
        scheduleTime = itemView.findViewById(R.id.schedule_time)
        scheduleCancel = itemView.findViewById(R.id.schedule_cancel)
        scheduleUpdate = itemView.findViewById(R.id.schedule_edit)
    }

    fun bind(item: AppLaunchSchedule) {
        scheduleTime?.text = AppSchedulerUtils.getLaunchTime(item.launchTime)
        scheduleAppTitle?.text = AppSchedulerUtils.getAppTitle(
            itemView.context,
            item.packageName,
            item.className
        )
        scheduleAppIcon?.setImageDrawable(
            AppSchedulerUtils.getAppIcon(
                itemView.context,
                item.packageName,
                item.className
            )
        )
    }
}
