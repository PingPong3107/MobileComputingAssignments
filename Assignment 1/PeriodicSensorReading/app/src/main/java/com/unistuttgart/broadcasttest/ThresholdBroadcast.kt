package com.unistuttgart.broadcasttest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Broadcast receiver that listens for threshold events and shows a toast message.
 */
class ThresholdBroadcast: BroadcastReceiver(){

    /**
     * Show a toast message when a threshold is reached.
     * @param context The context in which the receiver is running.
     * @param intent The intent that triggered the receiver.
     * @see BroadcastReceiver.onReceive
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BackgroundService.ACTION_THRESHOLD_REACHED){
            val appContext = context?.applicationContext
            val sensor = intent.getStringExtra("sensor")
            val value = intent.getFloatExtra("value", 0.0f)
            val threshold = intent.getFloatExtra("threshold", 0.0f)
            val message = "$sensor with value $value exceeded threshold $threshold"
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}