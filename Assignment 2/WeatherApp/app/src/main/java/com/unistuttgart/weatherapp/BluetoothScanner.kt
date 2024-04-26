package com.unistuttgart.weatherapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService

class BluetoothScanner (context: Context) {
    companion object{
        private const val TAG = "BluetoothScanner"
    }
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var handler = Handler(Looper.getMainLooper())
    private val stopScanRunnable = Runnable {
        stopScan()
    }

    init{
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(context, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
        }
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    @SuppressLint("MissingPermission")
    fun startScan(){
        bluetoothLeScanner?.let {
            it.startScan(scanCallback)
            Log.i(TAG, "Scan started")
            handler.postDelayed(stopScanRunnable, 10000)
        }?: Log.e(TAG, "BluetoothLeScanner not initialized")
    }

    @SuppressLint("MissingPermission")
    fun stopScan(){
        bluetoothLeScanner?.let {
            it.stopScan(scanCallback)
            Log.i(TAG, "Scan stopped")
            handler.removeCallbacks(stopScanRunnable)
        }?: Log.e(TAG, "BluetoothLeScanner not initialized")
    }

    private val scanCallback = object: ScanCallback(){
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device: BluetoothDevice = result.device
            Log.i(TAG, "Found device: ${device.name} ${device.address}")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code $errorCode")
        }
    }
}