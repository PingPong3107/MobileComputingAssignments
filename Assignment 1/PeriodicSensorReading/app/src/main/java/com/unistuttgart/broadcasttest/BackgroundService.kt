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

    var currentLightValue: Float = 0.0f
    var currentMagneticValue: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)
    var currentAccelerationValue: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)

    var lightValue: MutableLiveData<Float> = MutableLiveData()
    var magneticValue: MutableLiveData<FloatArray> = MutableLiveData()
    var accelerationValue: MutableLiveData<FloatArray> = MutableLiveData()

    var compassAngle: MutableLiveData<Double> = MutableLiveData()

    var thresholdService1: Float = 100000.0f
    var thresholdService2: Float = 100000.0f
    var thresholdService1Reached= false
    var thresholdService2Reached= false

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                when (it.sensor.type) {
                    Sensor.TYPE_LIGHT -> currentLightValue=it.values[0]
                    Sensor.TYPE_MAGNETIC_FIELD -> currentMagneticValue=it.values
                    Sensor.TYPE_ACCELEROMETER  -> currentAccelerationValue=it.values
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
                    lightValue.postValue(currentLightValue)
                    magneticValue.postValue(currentMagneticValue)
                    accelerationValue.postValue(currentAccelerationValue)
                    compassAngle.postValue(compass())

                    if (lightValue.value != null){
                        if (!thresholdService1Reached && lightValue.value!! > thresholdService1) {
                            thresholdService1Reached = true
                            val intent = Intent(ACTION_THRESHOLD_REACHED)
                            intent.putExtra("sensor", "Light")
                            intent.putExtra("value", lightValue.value)
                            intent.putExtra("threshold", thresholdService1)
                            sendBroadcast(intent)
                        } else if(lightValue.value!! < thresholdService1){
                            thresholdService1Reached = false
                        }
                    }

                    if (compassAngle.value != null){
                        val angle = (compassAngle.value!! + 360) % 360
                        if(!thresholdService2Reached && angle > thresholdService2){
                            thresholdService2Reached = true
                            val intent = Intent(ACTION_THRESHOLD_REACHED)
                            intent.putExtra("sensor", "Angle")
                            intent.putExtra("value", angle.toFloat())
                            intent.putExtra("threshold", thresholdService2)
                            sendBroadcast(intent)
                        } else if(angle < thresholdService2){
                            thresholdService2Reached = false
                        }
                    }

                }
            }, 0, period)
        }
    }

    fun compass(): Double{
        val identityMatrix = FloatArray(9)
        val rotationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(
            rotationMatrix, identityMatrix,
            currentAccelerationValue, currentMagneticValue
        )
        if (success) {
            val orientationMatrix = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationMatrix)
            val rotationInRadians = orientationMatrix[0]
            return Math.toDegrees(rotationInRadians.toDouble())
        }
        return 0.0
    }

    companion object {
        const val ACTION_THRESHOLD_REACHED = "ACTION_THRESHOLD_REACHED"
    }
}