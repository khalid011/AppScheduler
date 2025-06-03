package com.khalid.appscheduler

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.khalid.appscheduler.common.logger.AppScheduleLog
import com.khalid.appscheduler.databinding.ActivityMainBinding
import com.khalid.appscheduler.ui.modifySchedule.ModifyScheduleActivity
import com.khalid.appscheduler.utils.AppSchedulerUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSchedulerUtils.createNotificationChannel(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_schedule_history
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        initFabButtonListener()
    }

    private fun launchAddSchedule() {
        val intent = Intent(this, ModifyScheduleActivity::class.java)
        intent.putExtra(AppSchedulerUtils.KEY_ADD_SCHEDULE, true)
        startActivity(intent)
    }

    private fun initFabButtonListener() {
        binding.fabScheduleUpdate.setOnClickListener {
            AppScheduleLog.d(TAG, "[initFabButtonListener] add schedule in progress...")
            launchAddSchedule()
        }
    }

}