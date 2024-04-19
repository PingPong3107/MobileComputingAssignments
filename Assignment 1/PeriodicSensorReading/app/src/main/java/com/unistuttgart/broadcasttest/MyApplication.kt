package com.unistuttgart.broadcasttest

import android.app.Application
import android.content.ComponentName
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi

class MyApplication : Application() {

    private val thresholdBroadcast = ThresholdBroadcast()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()

        registerReceiver(thresholdBroadcast, IntentFilter(BackgroundService.ACTION_THRESHOLD_REACHED),
            RECEIVER_EXPORTED)
    }

    override fun onTerminate() {
        super.onTerminate()

        unregisterReceiver(thresholdBroadcast)
    }

}