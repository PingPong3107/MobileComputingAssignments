package de.unistuttgart.gstest

import android.os.Bundle
import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import de.unistuttgart.gstest.gpsservice.MyLocationListener
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
        ContextCompat.registerReceiver(
            this,
            gpsBroadcastReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        findViewById<TextView>(R.id.latTextView).text = getString(R.string.latitudeTextView).format("")
        findViewById<TextView>(R.id.lngTextView).text = getString(R.string.longitudeTextView).format("")
        findViewById<TextView>(R.id.distanceTextView).text = getString(R.string.distanceTextView).format(0)
        findViewById<TextView>(R.id.averageSpeedTextView).text = getString(R.string.average_speedTextView).format(0)

        findViewById<Button>(R.id.updateValues).setOnClickListener {
            if (gpsBroadcastReceiver.waypointList.isEmpty()) {
                Toast.makeText(this, "No waypoints recorded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findViewById<TextView>(R.id.latTextView).text = getString(R.string.latitudeTextView).format(gpsBroadcastReceiver.waypointList.last().latitude)
            findViewById<TextView>(R.id.lngTextView).text = getString(R.string.longitudeTextView).format(gpsBroadcastReceiver.waypointList.last().longitude.toString())
            findViewById<TextView>(R.id.distanceTextView).text = getString(R.string.distanceTextView).format(calculateDistances().toString())
            findViewById<TextView>(R.id.averageSpeedTextView).text = getString(R.string.average_speedTextView).format(calculateAverageSpeed().toString())
        }
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

    private fun calculateDistances(): Double {
        var distance = 0.0
        gpsBroadcastReceiver.waypointList.forEachIndexed { index, waypoint ->
            if (index > 0) {
                val previousWaypoint = gpsBroadcastReceiver.waypointList[index - 1]
                distance += measure(
                    previousWaypoint.latitude,
                    previousWaypoint.longitude,
                    waypoint.latitude,
                    waypoint.longitude
                )
            }
        }
        return distance
    }

    private fun calculateAverageSpeed() :Double {
        val speed = 0.0
        if (gpsBroadcastReceiver.waypointList.size < 2){
            return speed
        }
        val distance = calculateDistances()
        val startTime = gpsBroadcastReceiver.waypointList.first().time.time
        val endTime = gpsBroadcastReceiver.waypointList.last().time.time
        val delta = (endTime - startTime) / 1000.0
        return distance/delta
    }

    /**
     * Calculate the distance between two points in latitude and longitude
     * Source: https://stackoverflow.com/questions/639695/how-to-convert-latitude-or-longitude-to-meters
     * Based on: https://en.wikipedia.org/wiki/Haversine_formula
     */
    private fun  measure(lat1:Double, lon1:Double, lat2:Double, lon2:Double):Double{
        val radius = 6378.137; // Radius of earth in KM
        val dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        val dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        val a = sin(dLat/2) * sin(dLat/2) +
                cos(lat1 * Math.PI / 180) * cos(lat2 * Math.PI / 180) *
                sin(dLon/2) * sin(dLon/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        val d = radius * c
        return d * 1000 // meters
    }
}

