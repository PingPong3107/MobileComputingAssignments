package com.unistuttgart.broadcasttest

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.Timer
import java.util.TimerTask
private val thresholdBroadcast = ThresholdBroadcast()

class BackgroundService: Service() {
    private val binder = LocalBinder()
    private lateinit var timer: Timer
    private var period: Long = 1000

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var magneticSensor: Sensor
    private lateinit var acceleroSensor: Sensor

    var testLightValue: MutableLiveData<Float> = MutableLiveData()
    var testMagneticValue: MutableLiveData<FloatArray> = MutableLiveData()
    var testAccelerationValue: MutableLiveData<FloatArray> = MutableLiveData()

    var lightValue: MutableLiveData<Float> = MutableLiveData()
    var magneticValue: MutableLiveData<FloatArray> = MutableLiveData()
    var accelerationValue: MutableLiveData<FloatArray> = MutableLiveData()

    var wingl: MutableLiveData<Double> = MutableLiveData()

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // You can handle changes in sensor accuracy here if needed
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                when (it.sensor.type) {
                    Sensor.TYPE_LIGHT -> testLightValue.postValue(it.values[0])
                    Sensor.TYPE_MAGNETIC_FIELD -> testMagneticValue.postValue(it.values)
                    Sensor.TYPE_ACCELEROMETER  -> testAccelerationValue.postValue(it.values)
                }
            }
        }
    }


    inner class LocalBinder : Binder() {
        fun getService(): BackgroundService = this@BackgroundService
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            thresholdBroadcast, IntentFilter(ACTION_THRESHOLD_REACHED)
        )
//        val broadcastIntent = Intent(ACTION_THRESHOLD_REACHED)
//        broadcastIntent.putExtra("event", "Startup")
//        broadcastIntent.putExtra("value", "Service started")
//        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
        acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL)


        startTimer()
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun modifyVariable(newValue: Long) {
        period = newValue
        startTimer()
    }

    private fun startTimer() {
        if (::timer.isInitialized) {
            timer.cancel()
        }
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    lightValue.postValue(testLightValue.value)
                    magneticValue.postValue(testMagneticValue.value)
                    accelerationValue.postValue(testAccelerationValue.value)
                    wingl.postValue(compass())
                }
            }, 0, period)
        }
    }

    fun compass(): Double{
        var returnValue = 1000.0
        if (testMagneticValue.value != null && testAccelerationValue.value != null) {
            returnValue = 2000.0
            val identityMatrix = FloatArray(9)
            val rotationMatrix = FloatArray(9)
            val success = SensorManager.getRotationMatrix(
                rotationMatrix, identityMatrix,
                testAccelerationValue.value, testMagneticValue.value
            )
            if (success) {
                returnValue = 3000.0
                val orientationMatrix = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationMatrix)
                val rotationInRadians = orientationMatrix[0]
                return Math.toDegrees(rotationInRadians.toDouble())
            }
        }
        return returnValue
    }

    companion object {
        const val ACTION_THRESHOLD_REACHED = "ACTION_THRESHOLD_REACHED"
    }
}