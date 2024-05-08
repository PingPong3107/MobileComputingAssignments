package com.unistuttgart.betterweatherscanner
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var listView: ListView
    private lateinit var adapter: DeviceAdapter
    private val devices = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        adapter = DeviceAdapter(this, android.R.layout.simple_list_item_1, devices)
        listView.adapter = adapter

        val scanButton: Button = findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            bluetoothManager.scanLeDevice(!bluetoothManager.isScanning)
            scanButton.text= if (bluetoothManager.isScanning) "Stop Scanning" else "Start Scanning"
        }

        val disconnectButton: Button = findViewById(R.id.disconnectButton)
        disconnectButton.setOnClickListener {
            bluetoothManager.disconnect()
        }



        bluetoothManager = BluetoothManager(this, object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.device?.let { device ->
                    if (device.name == null) return
                    val deviceInfo = "${device.name} (${device.address})"
                    if (deviceInfo !in devices) {
                        devices.add(deviceInfo)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val deviceAddress = devices[position].substringAfter("(").substringBefore(")")
            val device = bluetoothManager.bluetoothAdapter.getRemoteDevice(deviceAddress)
            //openFragment(WeatherConnectionFragment.newInstance())
            bluetoothManager.connectToDevice(this, device)
        }

        checkPermissions()
    }

    private fun openFragment(fragment: Fragment) {
        listView.visibility = View.GONE
        findViewById<Button>(R.id.scanButton).visibility = View.GONE
        findViewById<FrameLayout>(R.id.fragmentContainer).visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    @Suppress("DEPRECATION")
    @Deprecated("This method is deprecated, but I just don't care!")
    override fun onBackPressed() {
        val fragmentContainer: FrameLayout = findViewById(R.id.fragmentContainer)
        if (fragmentContainer.visibility == View.VISIBLE) {
            fragmentContainer.visibility = View.GONE
            listView.visibility = View.VISIBLE
            findViewById<Button>(R.id.scanButton).visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }



    private fun checkPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        requiredPermissions.forEach {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(it)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            finish()
        }
    }
}
