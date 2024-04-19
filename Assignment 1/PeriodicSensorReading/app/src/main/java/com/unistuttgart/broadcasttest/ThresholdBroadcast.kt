package com.unistuttgart.broadcasttest


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ThresholdBroadcast: BroadcastReceiver(){
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BackgroundService.ACTION_THRESHOLD_REACHED){
            val sensor = intent.getStringExtra("sensor")
            val value = intent.getFloatExtra("value", 0.0f)
            val threshold = intent.getFloatExtra("threshold", 0.0f)
            val message = "$sensor with value $value exceeded threshold $threshold"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}