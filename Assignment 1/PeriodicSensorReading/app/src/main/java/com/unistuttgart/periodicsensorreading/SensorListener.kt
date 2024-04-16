package com.unistuttgart.periodicsensorreading

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import java.util.Timer
import java.util.TimerTask

class SensorListener: Service() {
    private lateinit var timer: Timer
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var magneticSensor: Sensor

    var testLightValue: MutableLiveData<Float> = MutableLiveData()
    var testMagneticValue: MutableLiveData<Float> = MutableLiveData()

    var lightValue: MutableLiveData<Float> = MutableLiveData()
    var magneticValue: MutableLiveData<Float> = MutableLiveData()

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // You can handle changes in sensor accuracy here if needed
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                when (it.sensor.type) {
                    Sensor.TYPE_LIGHT -> testLightValue.postValue(it.values[0])
                    Sensor.TYPE_MAGNETIC_FIELD -> testMagneticValue.postValue(it.values[0])
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
        sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL)
        startTimer(1000)
        return LocalBinder()
    }


    fun startTimer(period: Long) {
        if (::timer.isInitialized) {
            timer.cancel()
        }
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    lightValue.postValue(testLightValue.value)
                    magneticValue.postValue(testMagneticValue.value)
                }
            }, 0, period)
        }
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): SensorListener = this@SensorListener
    }
}