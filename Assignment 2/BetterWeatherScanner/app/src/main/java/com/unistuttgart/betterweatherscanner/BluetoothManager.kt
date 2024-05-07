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

@SuppressLint("MissingPermission")
class BluetoothManager(context: Context, private val scanCallback: ScanCallback) {
    private val handler = Handler(Looper.getMainLooper())
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
                        Toast.makeText(context, "Connected to GATT server.", Toast.LENGTH_SHORT).show()
                    }
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i("BluetoothGattCallback", "Disconnected from GATT server.")
                    handler.post {
                        Toast.makeText(context, "Disconnected from GATT server.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                /**
                handler.post {
                    Toast.makeText(context, "GATT Services Discovered.", Toast.LENGTH_SHORT).show()
                }
                */
                gatt?.services?.forEach { service ->
                    /**
                    handler.post {
                        Toast.makeText(context, "Service UUID: ${service.uuid}", Toast.LENGTH_SHORT).show()
                    }
                    */

                    service.characteristics.forEach { characteristic ->
                        /**handler.post{
                            Toast.makeText(context, "Characteristic UUID: ${characteristic.uuid}", Toast.LENGTH_SHORT).show()
                        }


                        handler.post{
                            Toast.makeText(context, "Characteristic Properties: ${((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) }", Toast.LENGTH_SHORT).show()
                        }
                        */


                        if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                            gatt.readCharacteristic(characteristic)
                        }


                        // Subscribe if the characteristic supports notifications
                        if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            gatt.setCharacteristicNotification(characteristic, true)
                        }
                    }
                }
            } else {
                handler.post{
                    Toast.makeText(context, "onServicesDiscovered received: $status", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {

            handler.post {
                Toast.makeText(context, "Characteristic Changed: $value", Toast.LENGTH_SHORT).show()
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
            handler.post {
                Toast.makeText(context, "Characteristic Read: ${value.contentToString()}", Toast.LENGTH_SHORT).show()
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
}
