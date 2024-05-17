package de.unistuttgart.gstest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button
import de.unistuttgart.gstest.gpsservice.GpsBackgroundService

class UI (private val activity: Activity) {
    private val startButton: Button = activity.findViewById(R.id.startService)
    private val stopButton: Button = activity.findViewById(R.id.stopService)
    private val serviceIntent = Intent(activity, GpsBackgroundService::class.java)

    init{
        startButton.setOnClickListener {
            activity.startService(serviceIntent)
        }
        stopButton.setOnClickListener {
            activity.stopService(serviceIntent)
        }
    }
}