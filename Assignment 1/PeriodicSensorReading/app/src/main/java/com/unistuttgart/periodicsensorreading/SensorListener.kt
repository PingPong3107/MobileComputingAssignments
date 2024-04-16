package com.unistuttgart.periodicsensorreading

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import java.util.Timer
import java.util.TimerTask

class SensorListener: Service() {
    private lateinit var timer: Timer
    public var sensorValue: MutableLiveData<Float> = MutableLiveData()

    override fun onBind(intent: Intent?): IBinder {
        startTimer(1000)
        return LocalBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTimer(1000)
        return START_STICKY
    }

    public fun startTimer(period: Long) {
        if (::timer.isInitialized) {
            timer.cancel()
        }
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    sensorValue.postValue((0..100).random().toFloat())
                }
            }, 0, period)

        }
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): SensorListener = this@SensorListener
        }

}