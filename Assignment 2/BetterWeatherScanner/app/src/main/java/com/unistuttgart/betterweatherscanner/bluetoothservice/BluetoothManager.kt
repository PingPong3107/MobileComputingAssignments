package com.unistuttgart.betterweatherscanner.bluetoothservice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.unistuttgart.betterweatherscanner.BuildConfig
import com.unistuttgart.betterweatherscanner.DeviceAdapter

@SuppressLint("MissingPermission")
class BluetoothManager(private val context: Context, adapter: DeviceAdapter, devices:MutableList<String>) {

    private val scanCallback = ScanCallback(devices, adapter)
    var gattCallback = GattCallback(context)
    var bluetoothGatt: BluetoothGatt? = null

    val bluetoothAdapter: BluetoothAdapter by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter
    }
    private val bluetoothLeScanner: BluetoothLeScanner
        get() = bluetoothAdapter.bluetoothLeScanner

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
        close()
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        Log.i(BuildConfig.LOG_TAG, "Connecting to ${device.name}...")
        Toast.makeText(context, "Connecting to ${device.name}...", Toast.LENGTH_SHORT).show()
    }

    fun disconnect() {
        bluetoothGatt?.let { gatt ->
            gatt.disconnect()
            val intent = Intent(GattCallback.CONNECTSTATUS)
            intent.putExtra("address", gatt.device.address)
            intent.putExtra("status", "Disconnected")
            context.sendBroadcast(intent)
        }
    }

    fun close() {
        disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
