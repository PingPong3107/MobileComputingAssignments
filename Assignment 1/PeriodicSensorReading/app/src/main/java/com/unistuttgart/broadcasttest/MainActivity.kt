package com.unistuttgart.broadcasttest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unistuttgart.broadcasttest.databinding.ActivityMainBinding
import java.util.logging.Handler


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var backgroundService: BackgroundService
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BackgroundService.LocalBinder
            backgroundService = binder.getService()

            backgroundService.lightValue.observe(this@MainActivity) { value ->
                binding.light.text = "Light: $value"
            }
            backgroundService.compassAngle.observe(this@MainActivity) { value ->
                val angle = (value + 360) % 360
                binding.compass.text = "Orientation: ${Math.round(angle)}°"
                binding.circleView.setAngle(angle.toFloat())
            }
            backgroundService.magneticValue.observe(this@MainActivity) { value ->
                binding.magnetic.text = "Magneto:\n${value[0]}\n${value[1]}\n${value[2]}"
            }

            backgroundService.accelerationValue.observe(this@MainActivity) { value ->
                binding.accelero.text = "Accelero:\n${value[0]}\n${value[1]}\n${value[2]}"
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(ContextCompat.checkSelfPermission(this.applicationContext, "android.permission.POST_NOTIFICATIONS") == -1){
            requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 0)
        }

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)
        val toolbarColour = (binding.toolbar.background as ColorDrawable).color
        window.statusBarColor = toolbarColour


        val serviceIntent = Intent(this@MainActivity, BackgroundService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        binding.periodButton.setOnClickListener {
            backgroundService.modifyVariable(binding.periodInput.text.toString().toLong())
        }

        binding.ThresholdButton1.setOnClickListener {
            backgroundService.thresholdService1 = binding.ThresholdInput1.text.toString().toFloat()
            backgroundService.thresholdService1Reached = false
        }

        binding.ThresholdButton2.setOnClickListener {
            backgroundService.thresholdService2 = binding.ThresholdInput2.text.toString().toFloat()
            backgroundService.thresholdService2Reached = false
        }

        val looper = Looper.getMainLooper()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}