package com.unistuttgart.betterweatherscanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.unistuttgart.betterweatherscanner.bluetoothservice.BluetoothManager
import com.unistuttgart.betterweatherscanner.bluetoothservice.GattCallback

class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var listView: ListView
    private lateinit var adapter: DeviceAdapter
    private val devices = mutableListOf<String>()
    private var characteristics = MutableLiveData<MutableList<BluetoothGattCharacteristic>>()
    private lateinit var temperatureView: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            finish()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                GattCallback.HUMIDITY_CHANGE -> {
                    val humidity = intent.getStringExtra("humidity")
                    Log.i("BTBroadcastReceiver", "Humidity: $humidity")
                    // Use the humidity value here
                }
                GattCallback.TEMPERATURE_CHANGE -> {
                    val temperature = intent.getStringExtra("temperature")
                    Log.i("BTBroadcastReceiver", "Temperature: $temperature")
                    temperatureView.text = "Temperature: $temperature"
                    // Use the temperature value here
                }
                GattCallback.CONNECTSTATUS -> {
                    val status = intent.getStringExtra("status")
                    val address = intent.getStringExtra("address")
                    Log.i("BTBroadcastReceiver", "Address: $address, Status: $status")


                    if (address == "F8:20:74:F7:2B:82" && status == "Connected"){
                        val layout = findViewById<LinearLayout>(R.id.newLinearLayout)
                        val editText = EditText(this@MainActivity)
                        editText.inputType = InputType.TYPE_CLASS_NUMBER // Set the input type to number
                        editText.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        layout.addView(editText)

                        val button = Button(this@MainActivity)
                        button.text = "Set Value"
                        layout.addView(button)
                        button.setOnClickListener {
                            val number = editText.text.toString().toIntOrNull() ?: 0 // Get the number from the EditText
                            bluetoothManager.gattCallback.lightCharacteristic?.let {
                                it.setValue(bluetoothManager.gattCallback.uint16ToBytes(number)) // Use the number here
                                bluetoothManager.bluetoothGatt?.writeCharacteristic(it)
                            }
                        }
                    }

                    if (address == "F6:B6:2A:79:7B:5D" && status == "Connected"){
                        val layout = findViewById<LinearLayout>(R.id.newLinearLayout)
                        temperatureView = TextView(this@MainActivity)
                        temperatureView.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        layout.addView(temperatureView)

                    }
                    if(status == "Disconnected"){
                        val layout = findViewById<LinearLayout>(R.id.newLinearLayout)
                        layout.removeAllViews()
                    }


                    // Use the status and address values here
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val filter = IntentFilter().apply {
            addAction(GattCallback.HUMIDITY_CHANGE)
            addAction(GattCallback.TEMPERATURE_CHANGE)
            addAction(GattCallback.CONNECTSTATUS)
        }
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)



        checkPermissions()

        listView = findViewById(R.id.listView)
        adapter = DeviceAdapter(this, android.R.layout.simple_list_item_1, devices)
        listView.adapter = adapter

        bluetoothManager = BluetoothManager(this, adapter, devices, characteristics)

        setupButtonListeners()
        setupListViewListener()

        characteristics.observe(this) { characteristicsList ->

        }


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

    @SuppressLint("MissingPermission")
    private fun setupButtonListeners() {
        val scanButton: Button = findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            bluetoothManager.scanLeDevice(!bluetoothManager.isScanning)
            scanButton.text = if (bluetoothManager.isScanning) "Stop Scanning" else "Start Scanning"
            bluetoothManager.gattCallback.temperatureCharacteristic?.let { characteristic ->
                   bluetoothManager.bluetoothGatt?.readCharacteristic(characteristic)
            }
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
