package com.unistuttgart.broadcasttest

import android.app.Application
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Application class that registers the ThresholdBroadcast receiver.
 */
class MyApplication : Application() {

    private val thresholdBroadcast = ThresholdBroadcast()

    /**
     * Register the ThresholdBroadcast receiver when the application is created.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()

        registerReceiver(thresholdBroadcast, IntentFilter(BackgroundService.ACTION_THRESHOLD_REACHED),
            RECEIVER_EXPORTED)
    }

    /**
     * Unregister the ThresholdBroadcast receiver when the application is terminated.
     */
    override fun onTerminate() {
        super.onTerminate()

        unregisterReceiver(thresholdBroadcast)
    }

}