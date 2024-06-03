package de.unistuttgart.eddystonetest

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import de.unistuttgart.eddystonetest.ble.BluetoothManager

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothManager: BluetoothManager
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            finish()
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == BluetoothManager.BEACONDATA) {
                val extras = intent.extras
                if (extras != null) {
                    for (key in extras.keySet()) {
                        when(key){
                            "Beacon Temperature" -> findViewById<TextView>(R.id.temperatureTextview).apply {
                                text = getString(R.string.beacon_id_textview).format(extras.get("Beacon Temperature").toString())
                            }
                            "BeaconID" -> findViewById<TextView>(R.id.beaconIDTextview).apply {
                                text = getString(R.string.beacon_id_textview).format(extras.get("BeaconID").toString())
                            }
                            "Distance" -> findViewById<TextView>(R.id.distanceTextview).apply {
                                text = getString(R.string.distance_textview).format(extras.get("Distance").toString().take(3))
                            }
                            "Battery Voltage" -> findViewById<TextView>(R.id.voltageTextview).apply {
                                text = getString(R.string.voltage_textview).format(extras.get("Battery Voltage").toString())
                            }
                            "URL" -> findViewById<TextView>(R.id.urlTextview).apply {
                                text = getString(R.string.url_textview).format(extras.get("URL").toString())
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        bluetoothManager = BluetoothManager(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.app_name)
        toolbar.setTitleTextColor(Color.BLACK)
        window.statusBarColor = (toolbar.background as ColorDrawable).color

        checkPermissions()
        setButtonListeners()
        resetCards()

        val filter = IntentFilter().apply {
            addAction(BluetoothManager.BEACONDATA)
        }
        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun resetCards() {
        findViewById<TextView>(R.id.temperatureTextview).apply {
            text = getString(R.string.beacon_id_textview).format("-")
        }
        findViewById<TextView>(R.id.beaconIDTextview).apply {
            text = getString(R.string.beacon_id_textview).format("-")
        }
        findViewById<TextView>(R.id.distanceTextview).apply {
            text = getString(R.string.distance_textview).format("-")
        }
        findViewById<TextView>(R.id.voltageTextview).apply {
            text = getString(R.string.voltage_textview).format("-")
        }
        findViewById<TextView>(R.id.urlTextview).apply {
            text = getString(R.string.url_textview).format("-")
        }
    }

    private fun setButtonListeners() {
        val scanButton = findViewById<Button>(R.id.scanButton)
        scanButton.setTextColor(Color.BLACK)
        scanButton.setOnClickListener {
            resetCards()
            bluetoothManager.scanLeDevice(bluetoothManager.isScanning)
            scanButton.text = if (!bluetoothManager.isScanning) this.getString(R.string.start_scan) else this.getString(R.string.stop_scan)

        }

    }
    private fun checkPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        }

        requiredPermissions.forEach {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(it)
            }
        }
    }
}