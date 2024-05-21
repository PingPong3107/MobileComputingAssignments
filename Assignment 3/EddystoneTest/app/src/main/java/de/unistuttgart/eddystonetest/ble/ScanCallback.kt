package de.unistuttgart.eddystonetest.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log

class ScanCallback : ScanCallback(){

    @SuppressLint("MissingPermission")
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        if (result != null) {
            if( result.device.address == "F6:B6:2A:79:7B:5D"){
                Log.i("ScanCallback", "Found device")
            }
        }
    }
}