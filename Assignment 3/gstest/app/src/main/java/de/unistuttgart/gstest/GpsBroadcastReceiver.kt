package de.unistuttgart.gstest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Environment
import android.util.Log
import de.unistuttgart.gstest.gpsservice.MyLocationListener
import me.himanshusoni.gpxparser.GPXWriter
import me.himanshusoni.gpxparser.modal.GPX
import me.himanshusoni.gpxparser.modal.Track
import me.himanshusoni.gpxparser.modal.TrackSegment
import me.himanshusoni.gpxparser.modal.Waypoint
import java.io.File
import java.io.FileOutputStream
import java.util.Date


class GpsBroadcastReceiver: BroadcastReceiver() {
    private val gpxWriter= GPXWriter()
    private val gpxFile = GPX()
    private lateinit var track : Track
    private lateinit var trackSegment: TrackSegment

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {

                MyLocationListener.LOCATION_CHANGED -> locationChanged(
                    intent.getParcelableExtra("location")!!
                )
                MyLocationListener.SERVICE_STARTSTOP -> {
                    val status = intent.getIntExtra("status", -1)
                    if (status == 1) {
                        Log.i("GpsBroadcastReceiver", "Service started")
                        trackSegment = TrackSegment()
                        track = Track()
                        gpxFile.addTrack(track)
                        track.addTrackSegment(trackSegment)
                    } else if (status == 0) {
                        Log.i("GpsBroadcastReceiver", "Service stopped")
                        saveGPX()
                    }
                }
            }
        }
    }
    private fun locationChanged(location: Location) {
        Log.i("GpsBroadcastReceiver", "Location changed: $location")
        val waypoint = Waypoint(location.latitude, location.longitude)
        val date = Date(location.time)
        waypoint.time = date
        trackSegment.addWaypoint(waypoint)
    }

    private fun saveGPX(){
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "outFile.gpx")
        val out = FileOutputStream(file)
        gpxWriter.writeGPX(gpxFile,out)
        out.close()
    }
}