package de.unistuttgart.gstest.gpsservice

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat

class MyLocationManager(private val context: Context) {
    private var locationManager: LocationManager? = null
    private val locationListener: MyLocationListener

    init {
        locationManager = ContextCompat.getSystemService(context, LocationManager::class.java)
        locationListener = MyLocationListener(context)
        enableGps()
    }

    private fun isGpsEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    private fun enableGps() {
        if (!isGpsEnabled()) {
            val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(enableGpsIntent)
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, locationListener)

    }

    fun removeLocationUpdates() {
        locationManager?.removeUpdates(locationListener)
    }

}