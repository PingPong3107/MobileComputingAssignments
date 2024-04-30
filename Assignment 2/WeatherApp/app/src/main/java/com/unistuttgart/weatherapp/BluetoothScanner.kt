package com.unistuttgart.weatherapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.os.ParcelUuid
import android.widget.Toast
import com.unistuttgart.weatherapp.databinding.ActivityMainBinding

@SuppressLint("MissingPermission")
class BluetoothScanner (private var context: Context, binding: ActivityMainBinding) {
    private var bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothScanner: BluetoothLeScanner? = bluetoothManager.adapter.bluetoothLeScanner

    private var gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
    }


    fun startScan(callback:ScanCallback) {
        bluetoothScanner?.startScan(callback)
    }

}