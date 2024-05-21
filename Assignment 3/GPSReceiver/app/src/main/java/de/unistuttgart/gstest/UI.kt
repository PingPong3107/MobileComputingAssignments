package de.unistuttgart.gstest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button
import de.unistuttgart.gstest.gpsservice.GpsBackgroundService

class UI (private val activity: Activity, private val context: Context) {
    private val startButton: Button = activity.findViewById(R.id.startService)
    private val serviceIntent = Intent(activity, GpsBackgroundService::class.java)
    private var isRunning = false

    init{
        startButton.setOnClickListener {
            if(!isRunning){
                activity.startService(serviceIntent)
                startButton.text = context.getString(R.string.stop_button_text)
                isRunning = true
            }
            else{
                activity.stopService(serviceIntent)
                startButton.text = context.getString(R.string.start_service)
                isRunning = false
            }
        }
    }
}