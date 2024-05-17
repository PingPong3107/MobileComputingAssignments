package de.unistuttgart.gstest.gpsservice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import de.unistuttgart.gstest.MainActivity
import de.unistuttgart.gstest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GpsBackgroundService: Service() {
    private var locationManager: MyLocationManager? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationManager = MyLocationManager(this)
        serviceScope.launch {
            withContext(Dispatchers.Main) {
                locationManager?.requestLocationUpdates()
            }
        }

        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_MUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GpsBackgroundService")
            .setContentText("Running...")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)


        val startupIntent = Intent(MyLocationListener.SERVICE_STARTSTOP)
        startupIntent.putExtra("status", 1)
        this.sendBroadcast(startupIntent)

        return START_STICKY
    }

    override fun onDestroy() {
        locationManager?.removeLocationUpdates()
        serviceScope.cancel()
        val stopIntent = Intent(MyLocationListener.SERVICE_STARTSTOP)
        stopIntent.putExtra("status", 0)
        this.sendBroadcast(stopIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "GpsBackgroundService Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
    companion object {
        const val CHANNEL_ID = "GpsBackgroundServiceChannel"
    }
}