package com.unistuttgart.betterweatherscanner
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

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

        val button: Button = findViewById(R.id.scanButton)
        button.setOnClickListener {
            bluetoothManager.scanLeDevice(!bluetoothManager.isScanning)
            button.text= if (bluetoothManager.isScanning) "Stop Scanning" else "Start Scanning"
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
            bluetoothManager.connectToDevice(this, device)
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
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
