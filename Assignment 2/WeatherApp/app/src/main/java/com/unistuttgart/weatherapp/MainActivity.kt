package com.unistuttgart.weatherapp

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unistuttgart.weatherapp.databinding.ActivityMainBinding
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.UUID

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var binding: ActivityMainBinding
    private val bluetoothPermissionRequestCode = 69420
    private lateinit var bluetoothScanner: BluetoothScanner
    private var devices = mutableSetOf<BluetoothDevice>()
    private var weatherGatt: BluetoothGatt? = null
    private var fanGatt: BluetoothGatt? = null

    private val WEATHER_SERVICE_UUID = UUID.fromString("00000002-0000-0000-FDFD-FDFDFDFDFDFD")

    private var callback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (!devices.contains(device)) {
                devices.add(device)
                addDeviceButton(device)
            }
        }
    }

    private val weatherGattCallback:BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Toast.makeText(this@MainActivity, "zumindest mal drinne", Toast.LENGTH_SHORT).show()
            if(newState == BluetoothGatt.STATE_CONNECTED) {
               Toast.makeText(this@MainActivity, "Connected to Weather Station", Toast.LENGTH_SHORT).show()
               gatt?.discoverServices()
           }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Toast.makeText(this@MainActivity, "Services discovered", Toast.LENGTH_SHORT).show()
            super.onServicesDiscovered(gatt, status)
            val characteristics = gatt?.getService(WEATHER_SERVICE_UUID)?.characteristics
            characteristics?.forEach {
                Toast.makeText(this@MainActivity, "Characteristic: ${it.uuid}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkBluetoothPermissions()

        bluetoothScanner = BluetoothScanner(this, binding)
        bluetoothScanner.startScan(callback)
    }


    private fun addDeviceButton(device: BluetoothDevice) {
        val button = Button(this)
        button.text = device.name ?: device.address
        when (device.address) {
            "F6:B6:2A:79:7B:5D" -> {
                button.text = "Weather Station"
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                button.setOnClickListener {
                    val gatt = device.connectGatt(this, false, weatherGattCallback)
                    // Speichern Sie das BluetoothGatt-Objekt für zukünftige Interaktionen
                    weatherGatt = gatt

                }
                binding.deviceContainer.addView(button)
            }
            /**
            "F8:20:74:F7:2B:82" -> {
                button.text = "Fan"
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
                button.setOnClickListener {
                    val gatt = device.connectGatt(this, false, fanGattCallback)
                    // Speichern Sie das BluetoothGatt-Objekt für zukünftige Interaktionen
                    fanGatt = gatt
                }
            }
            */
            else -> {
                button.setOnClickListener {
                    Toast.makeText(this, "Ich mach garrrnix.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //binding.deviceContainer.addView(button)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == bluetoothPermissionRequestCode) {

            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
                // All Bluetooth permissions have been granted
            } else {
                Toast.makeText(this, "Bluetooth permissions not granted", Toast.LENGTH_SHORT).show()
                // Permissions not granted. You could update your UI to disable features or inform the user.
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromDevice()
    }


    private fun disconnectFromDevice() {
        weatherGatt?.disconnect()
        weatherGatt?.close()
        fanGatt?.disconnect()
        fanGatt?.close()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ){

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                bluetoothPermissionRequestCode
            )
        } else {
            Toast.makeText(this, "Bluetooth permissions already granted", Toast.LENGTH_SHORT).show()
            // Permissions already granted - you can initiate Bluetooth scanning and connecting here
        }
    }
}