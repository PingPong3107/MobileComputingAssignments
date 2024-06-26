package de.unistuttgart.gstest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import de.unistuttgart.gstest.gpsservice.MyLocationListener
import me.himanshusoni.gpxparser.GPXWriter
import me.himanshusoni.gpxparser.modal.GPX
import me.himanshusoni.gpxparser.modal.Track
import me.himanshusoni.gpxparser.modal.TrackSegment
import me.himanshusoni.gpxparser.modal.Waypoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class GpsBroadcastReceiver(private val context: Context): BroadcastReceiver() {
    private val gpxFile = GPX()
    private lateinit var track : Track
    private lateinit var trackSegment: TrackSegment
    var waypointList = mutableListOf<Waypoint>()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {

                MyLocationListener.LOCATION_CHANGED -> {locationChanged(
                    intent.getParcelableExtra("location")!!
                )
                }
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
                        gpxFile.tracks.clear()
                        waypointList.clear()
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
        waypointList.add(waypoint)
    }

    private fun saveGPX(){
        if(waypointList.isNotEmpty()){
            val gpxWriter = GPXWriter()
            val formatter: SimpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.GERMANY)
            val now = Date()
            val fileName: String = "outFile_" + formatter.format(now) + ".gpx"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            val out = FileOutputStream(file)
            gpxWriter.writeGPX(gpxFile, out)
            out.close()

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, context.getString(R.string.store).format(fileName), Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, context.getString(R.string.no_store), Toast.LENGTH_SHORT).show()
            }
        }

    }
}