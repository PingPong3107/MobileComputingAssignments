package de.unistuttgart.eddystonetest.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.ParcelUuid
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ScanCallback : ScanCallback(){

    @SuppressLint("MissingPermission")
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.let {
            if (it.device.address == "F6:B6:2A:79:7B:5D") {
                // Filter for service id 0xFEAA
                val serviceData = it.scanRecord!!.getServiceData(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"))
                serviceData?.let { data ->
                    readFrames(data)
                }
            }
        }
    }

    private fun readFrames(bytes: ByteArray){
        when(bytes[0]){
            0x00.toByte() -> decodeUIDFrame(bytes)
            0x10.toByte() -> decodeURLFrame(bytes)
            0x20.toByte() -> decodeTLMFrame(bytes)
            else -> Log.i("ScanCallback", "Frame type: Unknown")
        }
    }

    private fun decodeUIDFrame(bytes: ByteArray){
        val txPower = bytes[1]
        val namespace = bytes.sliceArray(2..11)
        val instance = bytes.sliceArray(12..17)
        Log.i("ScanCallback", "Frame type: UID")
        Log.i("ScanCallback", "TX Power: $txPower")
        Log.i("ScanCallback", "Namespace: ${namespace.joinToString("") { it.toString(16) }}")
        Log.i("ScanCallback", "Instance: ${instance.joinToString("") { it.toString(16) }}")

    }

    private fun decodeURLFrame(bytes: ByteArray){
        val txPower = bytes[1]
        val urlScheme = bytes[2]
        val url = bytes.sliceArray(3 until bytes.size)
        Log.i("ScanCallback", "Frame type: URL")
        Log.i("ScanCallback", "TX Power: $txPower")
        Log.i("ScanCallback", "URL Scheme: $urlScheme")
        Log.i("ScanCallback", "URL: ${url.joinToString("") { it.toInt().toChar().toString() }}")
    }

    private fun decodeTLMFrame(bytes: ByteArray){
        val version = bytes[1]
        val voltage = bytes.sliceArray(2..3)
        val temp = bytes.sliceArray(4..5)
        Log.i("ScanCallback", "Frame type: TLM")
        Log.i("ScanCallback", "Version: $version")
        Log.i("ScanCallback", "Battery Voltage: ${bytesToUInt16(voltage)}")
        Log.i("ScanCallback", "Beacon Temperature: ${fixedPointToDouble(temp)}")
    }

    private fun bytesToUInt16(bytes: ByteArray): Int {
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(ByteOrder.BIG_ENDIAN) // Use LITTLE_ENDIAN if the bytes are in little-endian order
        return buffer.short.toInt() and 0xFFFF
    }

    private fun fixedPointToDouble(byteArray: ByteArray): Double {
        val integerPart = byteArray[0]
        val fractionalPart = byteArray[1]/ 256.0
        return integerPart + fractionalPart
    }

}