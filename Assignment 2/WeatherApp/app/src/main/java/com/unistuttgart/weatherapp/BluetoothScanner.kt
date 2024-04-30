package com.unistuttgart.weatherapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter.LeScanCallback
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

    //private var callback: LeScanCallback = LeScanCallback { device, rssi, scanRecord ->

        // Do something with the device and the RSSI
    //}

    private var callback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            // Do something with the device and the RSSI
            val address = result.device.address
            val specificUuid = ParcelUuid.fromString("00000002-0000-0000-FDFD-FDFDFDFDFDFD")

            if (address == "F6:B6:2A:79:7B:5D") {
                Toast.makeText(context, "Found the device™", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun startScan() {
        bluetoothScanner?.startScan(callback)
    }

}