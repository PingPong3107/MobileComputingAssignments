package de.unistuttgart.gstest

import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import de.unistuttgart.gstest.gpsservice.GpsBackgroundService
import de.unistuttgart.gstest.gpsservice.MyLocationListener
import de.unistuttgart.gstest.gpsservice.MyLocationManager

class MainActivity : AppCompatActivity() {
    private lateinit var ui: UI
    private val gpsBroadcastReceiver = GpsBroadcastReceiver()
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                ui = UI(this)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkPermissions()

        val filter = IntentFilter().apply {
            addAction(MyLocationListener.LOCATION_CHANGED)
            addAction(MyLocationListener.SERVICE_STARTSTOP)
        }
        ContextCompat.registerReceiver(this, gpsBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }


    private fun checkPermissions(){
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                ui = UI(this)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

