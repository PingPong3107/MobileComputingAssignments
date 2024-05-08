package com.unistuttgart.betterweatherscanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressLint("MissingPermission")
class BluetoothManager(context: Context, private val scanCallback: ScanCallback) {
    private val handler = Handler(Looper.getMainLooper())
    val gattServiceNames = mapOf(
        "00002a6f-0000-1000-8000-00805f9b34fb" to "Humidity",
        "00002a1c-0000-1000-8000-00805f9b34fb" to "Temperature",
    )


    val bluetoothAdapter: BluetoothAdapter by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter
    }
    private val bluetoothLeScanner: BluetoothLeScanner
        get() = bluetoothAdapter.bluetoothLeScanner

    private var bluetoothGatt: BluetoothGatt? = null

    var isScanning = false
        private set

    init {
        if (!bluetoothAdapter.isEnabled) {
            context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    fun scanLeDevice(enable: Boolean) {
        when {
            enable -> {
                isScanning = true
                bluetoothLeScanner.startScan(scanCallback)
            }

            else -> {
                isScanning = false
                bluetoothLeScanner.stopScan(scanCallback)
            }
        }
    }

    fun connectToDevice(context: Context, device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        Toast.makeText(context, "Connecting to ${device.name}...", Toast.LENGTH_SHORT).show()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i("BluetoothGattCallback", "Connected to GATT server.")
                    handler.post {
                        Toast.makeText(context, "Connected to GATT server.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i("BluetoothGattCallback", "Disconnected from GATT server.")
                    handler.post {
                        Toast.makeText(
                            context,
                            "Disconnected from GATT server.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt?.services?.forEach { service ->

                    if (service.uuid.toString() == "00000002-0000-0000-fdfd-fdfdfdfdfdfd") {
                        service.characteristics.forEach { characteristic ->

                            if (gattServiceNames.containsKey(characteristic.uuid.toString())) {
                                when (gattServiceNames[characteristic.uuid.toString()]) {
                                    "Temperature" -> {
                                        gatt.readCharacteristic(characteristic)
                                    }

                                    "Humidity" -> {
                                        gatt.readCharacteristic(characteristic)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                handler.post {
                    Toast.makeText(
                        context,
                        "onServicesDiscovered received: $status",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        @SuppressLint("NewApi")
        @Suppress("DEPRECATION")
        @Deprecated(
            "Used natively in Android 12 and lower",
            ReplaceWith("onCharacteristicChanged(gatt, characteristic, characteristic.value)")
        )
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) = onCharacteristicChanged(gatt, characteristic, characteristic.value)


        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handler.post {
                Toast.makeText(
                    context,
                    "Characteristic Changed: ${decodeTemperatureMeasurement(value)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        @SuppressLint("NewApi")
        @Suppress("DEPRECATION")
        @Deprecated(
            "Used natively in Android 12 and lower",
            ReplaceWith("onCharacteristicRead(gatt, characteristic, characteristic.value, status)")
        )
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) = onCharacteristicRead(gatt, characteristic, characteristic.value, status)


        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            handler.post { Toast.makeText(context, "Characteristic: ${gattServiceNames[characteristic.uuid.toString()]}", Toast.LENGTH_SHORT).show()}
            when (gattServiceNames[characteristic.uuid.toString()]) {
                "Temperature" -> {
                    handler.post {
                        Toast.makeText(
                            context,
                            "Temperature: ${decodeTemperatureMeasurement(value)}°C",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                "Humidity" -> {
                    handler.post {
                        Toast.makeText(
                            context,
                            "Humidity: ${decodeHumidityMeasurement(value)}%",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    fun close() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }


    fun decodeTemperatureMeasurement(data: ByteArray): Double {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        // Read flags
        val flags = buffer.get()
        val a = buffer.get()
        val b = buffer.get()
        val c = buffer.get()
        val d = buffer.get()

        val test = ByteArray(3)
        buffer.get(test,1,4)

        val hexString =
            byteArrayOf(c, b, a).joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
        return hexString.toInt(16) / 100.0
    }



    @OptIn(ExperimentalStdlibApi::class)
    private fun decodeHumidityMeasurement(data: ByteArray): Double {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        val a = buffer.get()
        val b = buffer.get()
        val hexString = byteArrayOf(b, a).joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
        return hexString.toDouble()
    }
}