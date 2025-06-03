package com.khalid.appscheduler.ui.scheduleHistory


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.R
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.repository.model.AppLaunchSchedule
import com.khalid.appscheduler.utils.AppSchedulerUtils

class AppScheduleHistoryAdapter(
    var scheduleList: MutableList<AppLaunchSchedule>
) : RecyclerView.Adapter<AppScheduleHistoryViewHolder>() {

    private val TAG = "AppScheduleAdapter"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppScheduleHistoryViewHolder {
        AppScheduleLog.d(TAG, "[onCreateViewHolder]")
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.schedule_info_history, parent, false)
        return AppScheduleHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppScheduleHistoryViewHolder, position: Int) {
        AppScheduleLog.d(TAG, "[onBindViewHolder] position: $position")
        holder.bind(scheduleList[position])
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }
}

class AppScheduleHistoryViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    var scheduleAppIcon: ImageView? = null
    var scheduleAppTitle: TextView? = null
    var scheduleTime: TextView? = null

    init {
        scheduleAppIcon = itemView.findViewById(R.id.scheduled_history_app_icon)
        scheduleAppTitle = itemView.findViewById(R.id.scheduled_history_app_title)
        scheduleTime = itemView.findViewById(R.id.schedule_history_time)
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
