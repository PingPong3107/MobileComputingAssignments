package com.unistuttgart.broadcasttest


import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.content.ContextCompat

class ThresholdBroadcast: BroadcastReceiver(){

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BackgroundService.ACTION_THRESHOLD_REACHED){
            val appContext = context?.applicationContext
            val sensor = intent.getStringExtra("sensor")
            val value = intent.getFloatExtra("value", 0.0f)
            val threshold = intent.getFloatExtra("threshold", 0.0f)
            val message = "$sensor with value $value exceeded threshold $threshold"
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
            println("Konsequenzen")
        }
    }
}