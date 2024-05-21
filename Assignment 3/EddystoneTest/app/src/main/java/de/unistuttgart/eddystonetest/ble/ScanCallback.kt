package de.unistuttgart.eddystonetest.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.util.Log
import de.unistuttgart.eddystonetest.MainActivity
import java.lang.Math.pow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow

class ScanCallback(private val context: Context) : ScanCallback(){

    @SuppressLint("MissingPermission")
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.let {
            if (it.device.address == "F6:B6:2A:79:7B:5D") {
                // Filter for service id 0xFEAA
                val serviceData = it.scanRecord!!.getServiceData(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"))
                serviceData?.let { data ->
                    readFrames(data, it.rssi)
                }
            }
        }
    }

    private fun readFrames(bytes: ByteArray, rssi: Int){
        when(bytes[0]){
            0x00.toByte() -> decodeUIDFrame(bytes, rssi)
            0x10.toByte() -> decodeURLFrame(bytes, rssi)
            0x20.toByte() -> decodeTLMFrame(bytes)
            else -> Log.i("ScanCallback", "Frame type: Unknown")
        }
    }

    private fun decodeUIDFrame(bytes: ByteArray, rssi: Int){
        val txPower = bytes[1]
        val namespace = bytes.sliceArray(2..11)
        val instance = bytes.sliceArray(12..17)
        Log.i("ScanCallback", "Frame type: UID")
        Log.i("ScanCallback", "TX Power: $txPower")
        Log.i("ScanCallback", "Namespace: ${namespace.joinToString("") { it.toString(16) }}")
        Log.i("ScanCallback", "Instance: ${instance.joinToString("") { it.toString(16) }}")
        Log.i("ScanCallback", "Distance: ${calculateDistance(rssi, txPower.toInt())}")

        val intent = Intent(BluetoothManager.BEACONDATA)
        intent.putExtra("BeaconID", instance.joinToString("") { it.toString(16) })
        intent.putExtra("Distance", calculateDistance(rssi, txPower.toInt()))
        context.sendBroadcast(intent)
    }

    private fun decodeURLFrame(bytes: ByteArray, rssi: Int){
        val txPower = bytes[1]
        val urlScheme = bytes[2]
        val url = bytes.sliceArray(3 until bytes.size)
        Log.i("ScanCallback", "Frame type: URL")
        Log.i("ScanCallback", "TX Power: $txPower")
        Log.i("ScanCallback", "URL Scheme: $urlScheme")
        Log.i("ScanCallback", "URL: ${url.joinToString("") { it.toInt().toChar().toString() }}")
        Log.i("ScanCallback", "Distance: ${calculateDistance(rssi, txPower.toInt())}")
        val intent = Intent(BluetoothManager.BEACONDATA)
        intent.putExtra("URL", url.joinToString("") { it.toInt().toChar().toString() })
        intent.putExtra("Distance", calculateDistance(rssi, txPower.toInt()))
        context.sendBroadcast(intent)
    }

    private fun decodeTLMFrame(bytes: ByteArray){
        val version = bytes[1]
        val voltage = bytes.sliceArray(2..3)
        val temp = bytes.sliceArray(4..5)
        Log.i("ScanCallback", "Frame type: TLM")
        Log.i("ScanCallback", "Version: $version")
        Log.i("ScanCallback", "Battery Voltage: ${bytesToUInt16(voltage)}")
        Log.i("ScanCallback", "Beacon Temperature: ${fixedPointToDouble(temp)}")
        val intent = Intent(BluetoothManager.BEACONDATA)
        intent.putExtra("Battery Voltage", bytesToUInt16(voltage))
        intent.putExtra("Beacon Temperature", fixedPointToDouble(temp))
        context.sendBroadcast(intent)
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

    private fun calculateDistance(rssi: Int, txPower: Int): Double {
        val txPowerAt1m = txPower - 40
        return 10.0.pow(-(rssi - txPowerAt1m) / (10 * 2.0))
    }

}