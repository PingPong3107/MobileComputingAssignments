package de.unistuttgart.gstest.gpsservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class GpsBackgroundService: Service() {
    private lateinit  var locationManager: MyLocationManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("GpsBackgroundService", "Service started")
        locationManager = MyLocationManager(this)
        locationManager.requestLocationUpdates()

        return START_STICKY
    }

    override fun onDestroy() {
        locationManager.removeLocationUpdates()
        Log.i("GpsBackgroundService", "Service stopped")
    }
}