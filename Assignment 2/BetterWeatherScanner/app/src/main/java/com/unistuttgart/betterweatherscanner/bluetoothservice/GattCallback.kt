package com.unistuttgart.betterweatherscanner.bluetoothservice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.unistuttgart.betterweatherscanner.BuildConfig
import com.unistuttgart.betterweatherscanner.ByteArrayDecoder
import java.util.UUID

@SuppressLint("MissingPermission")
class GattCallback: BluetoothGattCallback(){

    private var characteristicQueue = mutableListOf<Runnable>()
    private var byteArrayDecoder = ByteArrayDecoder()
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                Log.i(BuildConfig.LOG_TAG, "Connected to GATT server.")
                gatt?.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.i(BuildConfig.LOG_TAG, "Disconnected from GATT server.")
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
            for (service in gatt.services) {
                if (service.uuid.toString() == BuildConfig.WEATHER_SERVICE_UUID || service.uuid.toString() == BuildConfig.LIGHT_SERVICE_UUID){
                    for (characteristic in service.characteristics){
                        if (characteristic.uuid.toString() == BuildConfig.TEMPERATURE_CHARACTERISTIC_UUID){
                            Log.i(BuildConfig.LOG_TAG, "Temperature characteristic found")
                            characteristicQueue.add(kotlinx.coroutines.Runnable {
                                gatt.readCharacteristic(characteristic)
                            })
                            characteristicQueue.add(kotlinx.coroutines.Runnable {
                                gatt.setCharacteristicNotification(characteristic, true)
                                val descriptor = characteristic.getDescriptor(
                                    UUID.fromString(
                                        BuildConfig.DESCRIPTOR_UUID
                                    )
                                )
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(descriptor)
                            })

                        }
                        if (characteristic.uuid.toString() == BuildConfig.HUMIDITY_CHARACTERISTIC_UUID){
                            Log.i(BuildConfig.LOG_TAG, "Humidity characteristic found")
                            characteristicQueue.add(kotlinx.coroutines.Runnable {
                                gatt.readCharacteristic(characteristic)
                            })
                            characteristicQueue.add(kotlinx.coroutines.Runnable {
                                gatt.setCharacteristicNotification(characteristic, true)
                                val descriptor = characteristic.getDescriptor(
                                    UUID.fromString(
                                        BuildConfig.DESCRIPTOR_UUID
                                    )
                                )
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(descriptor)

                            })
                        }
                        if (characteristic.uuid.toString() == BuildConfig.LIGHT_CHARACTERISTIC_UUID){
                            Log.i(BuildConfig.LOG_TAG, "Light characteristic found")
                            characteristicQueue.add(kotlinx.coroutines.Runnable {
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
        if (characteristic.uuid.toString() == BuildConfig.TEMPERATURE_CHARACTERISTIC_UUID) {
            val temperature = byteArrayDecoder.decodeTemperatureMeasurement(value)
            Log.i(BuildConfig.LOG_TAG, "Temperature changed: $temperature")
        }
        if (characteristic.uuid.toString() == BuildConfig.HUMIDITY_CHARACTERISTIC_UUID) {
            val humidity = byteArrayDecoder.decodeHumidityMeasurement(value)
            Log.i(BuildConfig.LOG_TAG, "Humidity changed: $humidity")
        }

        runNextCharacteristic()
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
        if (characteristic.uuid.toString() == BuildConfig.TEMPERATURE_CHARACTERISTIC_UUID) {
            val temperature = byteArrayDecoder.decodeTemperatureMeasurement(value)
            Log.i(BuildConfig.LOG_TAG, "Array: ${value.contentToString()}")
            Log.i(BuildConfig.LOG_TAG, "Temperature: $temperature")
        }
        if (characteristic.uuid.toString() == BuildConfig.HUMIDITY_CHARACTERISTIC_UUID) {
            val humidity = byteArrayDecoder.decodeHumidityMeasurement(value)
            Log.i(BuildConfig.LOG_TAG, "Humidity: $humidity")
        }
        runNextCharacteristic()
    }

    private fun runNextCharacteristic() {
        if (characteristicQueue.isNotEmpty()) {
            val firstRunnable = characteristicQueue.first()
            firstRunnable.run()
            characteristicQueue.removeAt(0)
        }
    }
}