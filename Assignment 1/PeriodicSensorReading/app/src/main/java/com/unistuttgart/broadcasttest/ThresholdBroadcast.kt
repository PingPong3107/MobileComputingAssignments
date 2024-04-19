package com.unistuttgart.broadcasttest


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ThresholdBroadcast: BroadcastReceiver(){
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BackgroundService.ACTION_THRESHOLD_REACHED){
            val sensor = intent.getStringExtra("sensor")
            val value = intent.getFloatExtra("value", 0.0f)
            val threshold = intent.getFloatExtra("threshold", 0.0f)
            val message = "$sensor with value $value exceeded threshold $threshold"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            println("Konsequenzen")
        }
    }
}