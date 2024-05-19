package de.unistuttgart.gstest.gpsservice

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.util.Log
import me.himanshusoni.gpxparser.modal.Waypoint
import java.util.Date

class MyLocationListener(private val context:Context): LocationListener {
    override fun onLocationChanged(location: Location) {
        //Log.i("MyLocationListener", "Location changed: $location")
        val intent = Intent(LOCATION_CHANGED)
        intent.putExtra("location", location)
        context.sendBroadcast(intent)
    }

    override fun onProviderEnabled(provider: String) {
        Log.i("MyLocationListener", "Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.i("MyLocationListener", "Provider disabled: $provider")
    }

    companion object{
        const val LOCATION_CHANGED = "LOCATION_CHANGED"
        const val SERVICE_STARTSTOP = "SERVICE_STARTSTOP"
    }
}