package com.unistuttgart.betterweatherscanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattDescriptor
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
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.unistuttgart.betterweatherscanner.bluetoothservice.BluetoothManager
import com.unistuttgart.betterweatherscanner.bluetoothservice.GattCallback
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var listView: ListView
    private lateinit var adapter: DeviceAdapter
    private val devices = mutableListOf<String>()
    private lateinit var temperatureView: TextView
    private lateinit var humidityView: TextView
    private var subscribedToHumidity = false
    private var subscribedToTemperature = false

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
                GattCallback.HUMIDITY_CHANGE -> handleHumidityChange(intent)
                GattCallback.TEMPERATURE_CHANGE -> handleTemperatureChange(intent)
                GattCallback.CONNECTSTATUS -> handleConnectStatus(context, intent)
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

        bluetoothManager = BluetoothManager(this, adapter, devices)

        setupButtonListeners()
        setupListViewListener()


    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.close()
    }


    private fun handleHumidityChange(intent: Intent){
        val humidity = intent.getStringExtra("humidity")
        Log.i("BTBroadcastReceiver", getString(R.string.humidity_value, humidity))
        humidityView.text = getString(R.string.humidity_value, humidity)
    }

    private fun handleTemperatureChange(intent: Intent){
        val temperature = intent.getStringExtra("temperature")
        Log.i("BTBroadcastReceiver", getString(R.string.temperature_value, temperature))
        temperatureView.text = getString(R.string.temperature_value, temperature)
    }

    private fun handleConnectStatus(context: Context, intent: Intent){
        val status = intent.getStringExtra("status")
        val address = intent.getStringExtra("address")
        Log.i("BTBroadcastReceiver", "Address: $address, Status: $status")
        if( status == "Connected"){
            Toast.makeText(context, getString(R.string.connected_to_device_toast, address), Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(context, getString(R.string.disconnected_from_device_toast, address), Toast.LENGTH_SHORT).show()
        }
        updateConnectionUI(context, address, status)
    }

    private fun updateConnectionUI(context: Context, address: String?, status: String?){
        val layout = findViewById<LinearLayout>(R.id.newLinearLayout)
        if (status == "Connected"){
            when (address){
                "F8:20:74:F7:2B:82" -> setupNumberInput(context, layout)
                "F6:B6:2A:79:7B:5D" -> setupSensorReading(context, layout)
            }
        } else if (status == "Disconnected") {
            layout.removeAllViews()
            subscribedToHumidity = false
            subscribedToTemperature = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupNumberInput(context: Context, layout: LinearLayout){
        val editText = EditText(this@MainActivity)
        editText.inputType = InputType.TYPE_CLASS_NUMBER // Set the input type to number
        editText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.addView(editText)

        val button = Button(this@MainActivity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = getString(R.string.submit_fan_speed_button)
            setOnClickListener {
                val number = editText.text.toString().toIntOrNull()
                    ?: 0 // Get the number from the EditText
                bluetoothManager.gattCallback.lightCharacteristic?.let {
                    it.setValue(bluetoothManager.gattCallback.uint16ToBytes(number)) // Use the number here
                    bluetoothManager.bluetoothGatt?.writeCharacteristic(it)
                }
            }
        }
        layout.addView(button)

    }

    @SuppressLint("MissingPermission")
    private fun setupSensorReading(context: Context, layout: LinearLayout){
        temperatureView = TextView(this@MainActivity)
        temperatureView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        humidityView = TextView(this@MainActivity)
        humidityView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val gridLayout = GridLayout(this@MainActivity).apply {
            rowCount = 2
            columnCount = 2
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val readTemperatureCharacteristic = Button(this@MainActivity).apply {
            text = getString(R.string.read_temperature_button)
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
            }
            setOnClickListener {
                bluetoothManager.gattCallback.temperatureCharacteristic?.let { characteristic ->
                    bluetoothManager.bluetoothGatt?.readCharacteristic(characteristic)
                }
            }
        }

        val readHumidityCharacteristic = Button(this@MainActivity).apply {
            text = getString(R.string.read_humidity_button)
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
            }
            setOnClickListener {
                bluetoothManager.gattCallback.humidityCharacteristic?.let { characteristic ->
                    bluetoothManager.bluetoothGatt?.readCharacteristic(characteristic)
                }
            }
        }
        val subscribeToTemperatureCharacteristic = Button(this@MainActivity).apply {
            text = if (subscribedToTemperature) getString(R.string.unsubscribe_from_temperature_button) else getString(R.string.subscribe_to_temperature_button)
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
            }
            setOnClickListener {
                text = if (!subscribedToTemperature) getString(R.string.unsubscribe_from_temperature_button) else getString(R.string.subscribe_to_temperature_button)
                if (subscribedToTemperature) {
                    bluetoothManager.gattCallback.temperatureCharacteristic?.let { characteristic ->
                        bluetoothManager.bluetoothGatt?.setCharacteristicNotification(
                            characteristic,
                            false
                        )
                    }
                } else {

                    bluetoothManager.gattCallback.temperatureCharacteristic?.let { characteristic ->
                        bluetoothManager.bluetoothGatt?.setCharacteristicNotification(
                            characteristic,
                            true
                        )
                    }
                    val descriptor =
                        bluetoothManager.gattCallback.temperatureCharacteristic?.getDescriptor(
                            UUID.fromString(
                                BuildConfig.DESCRIPTOR_UUID
                            )
                        )
                    if (descriptor != null) {
                        descriptor.value =
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    }
                    bluetoothManager.bluetoothGatt?.writeDescriptor(descriptor)
                }
                subscribedToTemperature = !subscribedToTemperature
            }
        }

        val subscribeToHumidityCharacteristic = Button(this@MainActivity).apply {
            text = if (subscribedToHumidity) getString(R.string.unsubscribe_from_humidity_button) else getString(R.string.subscribe_to_humidity_button)
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
            }
            setOnClickListener {
                text = if (!subscribedToHumidity) getString(R.string.unsubscribe_from_humidity_button) else getString(R.string.subscribe_to_humidity_button)
                if(subscribedToHumidity){
                    bluetoothManager.gattCallback.humidityCharacteristic?.let { characteristic ->
                        bluetoothManager.bluetoothGatt?.setCharacteristicNotification(
                            characteristic,
                            false
                        )
                    }
                }

                else {
                    bluetoothManager.gattCallback.humidityCharacteristic?.let { characteristic ->
                        bluetoothManager.bluetoothGatt?.setCharacteristicNotification(
                            characteristic,
                            true
                        )
                    }
                    val descriptor =
                        bluetoothManager.gattCallback.humidityCharacteristic?.getDescriptor(
                            UUID.fromString(
                                BuildConfig.DESCRIPTOR_UUID
                            )
                        )
                    if (descriptor != null) {
                        descriptor.value =
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    }
                    bluetoothManager.bluetoothGatt?.writeDescriptor(descriptor)
                }
                subscribedToHumidity = !subscribedToHumidity
            }
        }

        gridLayout.addView(readTemperatureCharacteristic)
        gridLayout.addView(subscribeToTemperatureCharacteristic)
        gridLayout.addView(readHumidityCharacteristic)
        gridLayout.addView(subscribeToHumidityCharacteristic)
        layout.addView(temperatureView)
        layout.addView(humidityView)
        layout.addView(gridLayout)
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
            scanButton.text = if (bluetoothManager.isScanning) getString(R.string.stop_scan_button) else getString(R.string.start_scan_button)
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
