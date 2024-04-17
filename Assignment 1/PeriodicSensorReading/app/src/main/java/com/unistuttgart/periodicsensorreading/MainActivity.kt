package com.unistuttgart.periodicsensorreading

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unistuttgart.periodicsensorreading.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mService: SensorListener
    private var mBound: Boolean = false
    private var thresholdService1: Float = 100000.0f
    private var thresholdService2: Float = 100000.0f
    private var thresholdService1Reached= false
    private var thresholdService2Reached= false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        val toolbarColour = (binding.toolbar.background as ColorDrawable).color
        window.statusBarColor = toolbarColour

        val connection = object : ServiceConnection {

            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as SensorListener.LocalBinder
                mService = binder.getService()
                mBound = true

                mService.lightValue.observe(this@MainActivity) { value ->
                    binding.light.text = "Light: $value"
                    if (!thresholdService1Reached && value != null && value > thresholdService1) {
                        toast("Light value $value exceeds threshold $thresholdService1")
                        thresholdService1Reached = true
                    }
                }
                mService.wingl.observe(this@MainActivity) { value ->
                    if (value != null){
                        val angle = (value + 360) % 360
                        binding.compass.text = "Orientation: ${Math.round(angle)}°"
                        binding.circleView.setAngle(angle.toFloat())
                    }

//                    if (!thresholdService2Reached && value != null && value > thresholdService2) {
//                        toast("Magnetic Field value $value exceeds threshold $thresholdService2")
//                        thresholdService2Reached = true
//                    }
                }
                mService.magneticValue.observe(this@MainActivity) { value ->
                    if (value != null){
                        binding.magnetic.text = "Magneto:\n${value[0]}\n${value[1]}\n${value[2]}"
                    }
                }

                mService.accelerationValue.observe(this@MainActivity) { value ->
                    if (value != null){
                        binding.accelero.text = "Accelero:\n${value[0]}\n${value[1]}\n${value[2]}"
                    }
                }

            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                mBound = false
            }
        }

        Intent(this, SensorListener::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        binding.periodButton.setOnClickListener {
            if (mBound) {
                mService.startTimer(binding.periodInput.text.toString().toLong())
            } else {
                toast("Service not bound")
            }
        }

        binding.ThresholdButton1.setOnClickListener {
            thresholdService1 = binding.ThresholdInput1.text.toString().toFloat()
            thresholdService1Reached = false
        }

        binding.ThresholdButton2.setOnClickListener {
            thresholdService2 = binding.ThresholdInput2.text.toString().toFloat()
            thresholdService2Reached = false
        }

    }

    private fun toast(message: CharSequence)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    public fun getBinding(): ActivityMainBinding {
        return binding
    }
}