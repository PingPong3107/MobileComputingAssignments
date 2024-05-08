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
import kotlinx.coroutines.Runnable
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressLint("MissingPermission")
class BluetoothManager(context: Context, private val scanCallback: ScanCallback) {
    private val HUMIDITY_CHARACTERISTIC_UUID = "00002a6f-0000-1000-8000-00805f9b34fb"
    private val TEMPERATURE_CHARACTERISTIC_UUID = "00002a1c-0000-1000-8000-00805f9b34fb"
    private val WEATHER_SERVICE_UUID = "00000002-0000-0000-fdfd-fdfdfdfdfdfd"
    private val LIGHT_SERVICE_UUID = "00000001-0000-0000-fdfd-fdfdfdfdfdfd"
    private val LIGHT_CHARACTERISTIC_UUID = "10000001-0000-0000-fdfd-fdfdfdfdfdfd"
    private val LOG_TAG = "MatthiasKuenzer"
    private var characteristicReadQueue = mutableListOf<Runnable>()

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
        bluetoothGatt?.close()
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        Log.i(LOG_TAG, "Connecting to ${device.name}...")
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(LOG_TAG, "Connected to GATT server.")
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(LOG_TAG, "Disconnected from GATT server.")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                for (service in gatt.services) {
                    if (service.uuid.toString() == WEATHER_SERVICE_UUID || service.uuid.toString() == LIGHT_SERVICE_UUID){

                        for (characteristic in service.characteristics){
                            if (characteristic.uuid.toString() == TEMPERATURE_CHARACTERISTIC_UUID){
                                Log.i(LOG_TAG, "Temperature characteristic found")
                                characteristicReadQueue.add(Runnable {
                                    gatt.readCharacteristic(characteristic)
                                })

                            }
                            if (characteristic.uuid.toString() == HUMIDITY_CHARACTERISTIC_UUID){
                                Log.i(LOG_TAG, "Humidity characteristic found")
                                characteristicReadQueue.add(Runnable {
                                    gatt.readCharacteristic(characteristic)
                                })
                            }
                            if (characteristic.uuid.toString() == LIGHT_CHARACTERISTIC_UUID){
                                Log.i(LOG_TAG, "Light characteristic found")
                                characteristicReadQueue.add(Runnable {
                                    gatt.readCharacteristic(characteristic)
                                })
                            }
                        }
                    }

                }
                runNextCharacteristic()

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
            if (characteristic.uuid.toString() == TEMPERATURE_CHARACTERISTIC_UUID) {
                val temperature = decodeTemperatureMeasurement(value)
                Log.i(LOG_TAG, "Temperature: $temperature")
            }
            if (characteristic.uuid.toString() == HUMIDITY_CHARACTERISTIC_UUID) {
                val humidity = decodeHumidityMeasurement(value)
                Log.i(LOG_TAG, "Humidity: $humidity")
            }
            runNextCharacteristic()

        }
    }

    fun runNextCharacteristic() {
        if (characteristicReadQueue.isNotEmpty()) {
            val firstRunnable = characteristicReadQueue.first()
            firstRunnable.run()
            characteristicReadQueue.removeAt(0)
        }
    }


    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    fun close() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }


    fun decodeTemperatureMeasurement(data: ByteArray): Float {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)



        // Read flags
        val flags = buffer.get()
        val d = buffer.get()
        val c = buffer.get()
        val b = buffer.get()
        val a = buffer.get()

        val result = Float.fromBits((a.toInt() shl 1) or (d.toInt() shl 9) or (c.toInt() shl 17) or (b.toInt() shl 25)
        )

        return result
    }



    @OptIn(ExperimentalStdlibApi::class)
    private fun decodeHumidityMeasurement(data: ByteArray): Double {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        val a = buffer.get()
        val b = buffer.get()
        val hexString = byteArrayOf(b, a).joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
        return 0.0
    }
}
