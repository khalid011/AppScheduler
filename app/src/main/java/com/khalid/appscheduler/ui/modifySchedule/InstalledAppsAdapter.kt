package com.khalid.appscheduler.ui.modifySchedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.khalid.appscheduler.R
import com.khalid.appscheduler.repository.model.InstalledAppInfo
import com.khalid.appscheduler.listener.AppSelectionListener
import com.khalid.appscheduler.utils.AppSchedulerUtils

class InstalledAppsAdapter(val installedAppsList: List<InstalledAppInfo>, val listener: AppSelectionListener) :
    RecyclerView.Adapter<InstalledAppsViewHolder>() {
    private var TAG = "InstalledAppsAdapter"

    var selectedAppPosition: Int = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InstalledAppsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.installed_appinfo, parent, false)
        return InstalledAppsViewHolder(this, view)
    }

    override fun onBindViewHolder(holder: InstalledAppsViewHolder, position: Int) {

        if (selectedAppPosition == position) {
            holder.bind(installedAppsList.get(position), true)
        } else {
            holder.bind(installedAppsList.get(position), false)
        }

        holder.itemView.setOnClickListener {
            holder.installedAppRadio?.isChecked?.let { it -> holder.installedAppRadio?.isChecked = !it }
            notifyItemChanged(selectedAppPosition)
            selectedAppPosition = position
            notifyItemChanged(selectedAppPosition)
            listener.onConfirmAppSelection(
                installedAppsList.get(selectedAppPosition).packageName,
                installedAppsList.get(selectedAppPosition).className,
                holder.installedAppTitle?.text.toString()
            )
        }

    }

    override fun getItemCount(): Int {
        return installedAppsList.size
    }

}

class InstalledAppsViewHolder(adapter: InstalledAppsAdapter, itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    private var TAG = "InstalledAppsViewHolder"
    var installedAppIcon: ImageView? = null
    var installedAppTitle: TextView? = null
    var installedAppRadio: RadioButton? = null

    init {
        installedAppIcon = itemView.findViewById(R.id.installed_app_icon)
        installedAppTitle = itemView.findViewById(R.id.installed_app_title)
        installedAppRadio = itemView.findViewById(R.id.installed_app_radiobutton)
    }

    fun bind(item: InstalledAppInfo, isSelected: Boolean) {
        installedAppIcon?.setImageDrawable(AppSchedulerUtils.getAppIcon(itemView.context, item.packageName, item.className))
        installedAppTitle?.text = AppSchedulerUtils.getAppTitle(itemView.context, item.packageName, item.className)
        if (isSelected) {
            installedAppRadio?.isChecked = true
        } else {
            installedAppRadio?.isChecked = false

        }
    }

}