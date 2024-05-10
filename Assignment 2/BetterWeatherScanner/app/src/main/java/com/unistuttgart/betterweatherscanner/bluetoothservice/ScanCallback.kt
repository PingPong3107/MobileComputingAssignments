package com.unistuttgart.betterweatherscanner.bluetoothservice

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.unistuttgart.betterweatherscanner.DeviceAdapter

@SuppressLint("MissingPermission")
class ScanCallback(private val devices: MutableList<String>, private val adapter: DeviceAdapter): ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        result?.device?.let { device ->
            if (device.name == null) return
            val deviceInfo = "${device.name} (${device.address})"
            if (deviceInfo !in devices) {
                devices.add(deviceInfo)
                adapter.notifyDataSetChanged()
            }
        }
    }
}