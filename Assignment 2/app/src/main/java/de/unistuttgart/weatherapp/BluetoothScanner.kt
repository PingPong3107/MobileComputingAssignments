package de.unistuttgart.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat


class BluetoothScanner(context: Context) {

    private var context: Context
    private var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: android.bluetooth.BluetoothAdapter
    private var bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner
    init {
        this.context = context
        this.bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.bluetoothAdapter = bluetoothManager.adapter
        this.bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    private var scanCallback =
        LeScanCallback { device, rssi, scanRecord ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_CODE_BLUETOOTH_CONNECT)
            }
            Log.i("BluetoothScanner", "Device found: ${device.name}")
        }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startScan() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                REQUEST_CODE_BLUETOOTH_SCAN)
        }

        bluetoothLeScanner.startScan(scanCallback)

    }
    companion object {
        private const val REQUEST_CODE_BLUETOOTH_SCAN = 1
        private const val REQUEST_CODE_BLUETOOTH_CONNECT = 2
    }

}