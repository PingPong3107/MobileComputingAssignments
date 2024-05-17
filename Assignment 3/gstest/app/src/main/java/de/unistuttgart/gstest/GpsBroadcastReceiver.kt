package de.unistuttgart.gstest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.util.Log
import android.widget.Toast
import de.unistuttgart.gstest.gpsservice.MyLocationListener

class GpsBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {

                MyLocationListener.LOCATION_CHANGED -> {
                    val location = intent.getParcelableExtra<Location>("location")
                    Log.i("GpsBroadcastReceiver", "Location changed: $location")
                    if (location != null) {
                        Toast.makeText(context, "Location changed: ${location.latitude}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}