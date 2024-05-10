package com.unistuttgart.betterweatherscanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.unistuttgart.betterweatherscanner.bluetoothservice.BluetoothManager

class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var listView: ListView
    private lateinit var adapter: DeviceAdapter
    private val devices = mutableListOf<String>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            finish()
        }
    }

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            if (!it.value) {
                finish()
                return@registerForActivityResult
            }
        }
        // Handle the case where all permissions are granted
    }

    private fun checkPermissions2() {
        val requiredPermissions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (requiredPermissions.any { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            requestMultiplePermissionsLauncher.launch(requiredPermissions)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions2()

        listView = findViewById(R.id.listView)
        adapter = DeviceAdapter(this, android.R.layout.simple_list_item_1, devices)
        listView.adapter = adapter

        bluetoothManager = BluetoothManager(this, adapter, devices)

        setupButtonListeners()
        setupListViewListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.close()
    }

    private fun setupListViewListener() {
        listView.setOnItemClickListener { _, _, position, _ ->
            val deviceAddress = devices[position].substringAfter("(").substringBefore(")")
            val device = bluetoothManager.bluetoothAdapter.getRemoteDevice(deviceAddress)
            bluetoothManager.connectToDevice(this, device)
        }
    }

    private fun setupButtonListeners() {
        val scanButton: Button = findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            bluetoothManager.scanLeDevice(!bluetoothManager.isScanning)
            scanButton.text = if (bluetoothManager.isScanning) "Stop Scanning" else "Start Scanning"
        }

        val disconnectButton: Button = findViewById(R.id.disconnectButton)
        disconnectButton.setOnClickListener {
            bluetoothManager.disconnect()
        }
    }

    private fun checkPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        }

        requiredPermissions.forEach {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(it)
            }
        }
    }
}
