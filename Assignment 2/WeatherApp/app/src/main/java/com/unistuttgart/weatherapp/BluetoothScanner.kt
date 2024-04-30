package com.unistuttgart.weatherapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.widget.Toast

@SuppressLint("MissingPermission")
class BluetoothScanner (private var context: Context) {
    private var bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private var bluetoothScanner: BluetoothLeScanner? = bluetoothManager.adapter.bluetoothLeScanner

    //private var callback: LeScanCallback = LeScanCallback { device, rssi, scanRecord ->

        // Do something with the device and the RSSI
    //}

    private var callback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            // Do something with the device and the RSSI
            Toast.makeText(context, "Device found", Toast.LENGTH_SHORT).show()
        }
    }


    fun startScan() {
        bluetoothScanner?.startScan(callback)
    }

}