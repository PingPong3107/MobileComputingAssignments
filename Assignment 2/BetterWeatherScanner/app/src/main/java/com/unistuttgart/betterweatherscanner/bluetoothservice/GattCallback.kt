package com.unistuttgart.betterweatherscanner.bluetoothservice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.unistuttgart.betterweatherscanner.BuildConfig
import com.unistuttgart.betterweatherscanner.ByteArrayDecoder
import java.util.UUID
import kotlinx.coroutines.Runnable

@SuppressLint("MissingPermission")
class GattCallback (private val context: Context): BluetoothGattCallback(){
    private var characteristicQueue = mutableListOf<Runnable>()
    private var byteArrayDecoder = ByteArrayDecoder()
    var humidityCharacteristic: BluetoothGattCharacteristic? = null
    var temperatureCharacteristic: BluetoothGattCharacteristic? = null
    var lightCharacteristic: BluetoothGattCharacteristic? = null

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {

                val intent = Intent(CONNECTSTATUS)
                intent.putExtra("address", gatt?.device?.address)
                intent.putExtra("status", "Connected")
                context.sendBroadcast(intent)
                gatt?.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                humidityCharacteristic = null
                temperatureCharacteristic = null
                lightCharacteristic = null
                val intent = Intent(CONNECTSTATUS)
                intent.putExtra("address", gatt?.device?.address)
                intent.putExtra("status", "Disconnected")
                context.sendBroadcast(intent)
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
                            temperatureCharacteristic = characteristic
                            /**
                            characteristicQueue.add(Runnable {
                                gatt.readCharacteristic(characteristic)
                            })
                            characteristicQueue.add(Runnable {
                                gatt.setCharacteristicNotification(characteristic, true)
                                val descriptor = characteristic.getDescriptor(
                                    UUID.fromString(
                                        BuildConfig.DESCRIPTOR_UUID
                                    )
                                )
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(descriptor)
                            })
                                */

                        }
                        if (characteristic.uuid.toString() == BuildConfig.HUMIDITY_CHARACTERISTIC_UUID){
                            humidityCharacteristic = characteristic
                            characteristicQueue.add(Runnable {
                                gatt.readCharacteristic(characteristic)
                            })
                            characteristicQueue.add(Runnable {
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
                            lightCharacteristic = characteristic
                            /**

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                                //gatt.writeCharacteristic(characteristic, byteArrayOf(0x01), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                                Log.e(BuildConfig.LOG_TAG, "Android Version too new, not implemented")
                            } else{
                                characteristicQueue.add(Runnable {
                                    characteristic.setValue(uint16ToBytes(1000))
                                    gatt.writeCharacteristic(characteristic)
                                })
                            }
                             */
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
            val intent = Intent(TEMPERATURE_CHANGE)
            intent.putExtra("temperature", temperature.toString())
            context.sendBroadcast(intent)
        }
        if (characteristic.uuid.toString() == BuildConfig.HUMIDITY_CHARACTERISTIC_UUID) {
            val humidity = byteArrayDecoder.decodeHumidityMeasurement(value)
            val intent = Intent(HUMIDITY_CHANGE)
            intent.putExtra("humidity", humidity.toString())
            context.sendBroadcast(intent)
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
            val intent = Intent(TEMPERATURE_CHANGE)
            intent.putExtra("temperature", temperature.toString())
            context.sendBroadcast(intent)
        }
        if (characteristic.uuid.toString() == BuildConfig.HUMIDITY_CHARACTERISTIC_UUID) {
            val humidity = byteArrayDecoder.decodeHumidityMeasurement(value)
            val intent = Intent(HUMIDITY_CHANGE)
            intent.putExtra("humidity", humidity.toString())
            context.sendBroadcast(intent)
        }
        runNextCharacteristic()
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        Log.i(BuildConfig.LOG_TAG, "Characteristic written with status $status")
        runNextCharacteristic()
    }



    private fun runNextCharacteristic() {
        if (characteristicQueue.isNotEmpty()) {
            val firstRunnable = characteristicQueue.first()
            firstRunnable.run()
            characteristicQueue.removeAt(0)
        }
    }

    fun uint16ToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte()
        )
    }

    companion object {
        const val HUMIDITY_CHANGE = "ACTION_THRESHOLD_REACHED"
        const val TEMPERATURE_CHANGE = "ACTION_TEMPERATURE_REACHED"
        const val CONNECTSTATUS = "ACTION_CONNECT_STATUS"
    }
}