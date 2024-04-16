package com.unistuttgart.periodicsensorreading

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorManager
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


                // Observe the sensorValue
                mService.sensorValue.observe(this@MainActivity) { value ->
                    // Update your UI here with the new sensor value
                    toast("Sensor Value: $value")
                }
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                mBound = false
            }
        }

        Intent(this, SensorListener::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    private fun toast(message: CharSequence)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    public fun getBinding(): ActivityMainBinding {
        return binding
    }
}