package de.unistuttgart.eddystonetest.ble


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData


@SuppressLint("MissingPermission")
class BluetoothManager(private val context: Context) {
    private val scanCallback = ScanCallback(context)

    private val bluetoothAdapter: BluetoothAdapter by lazy {
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

    fun scanLeDevice(enabled: Boolean) {
        when {
            enabled -> {
                isScanning = false
                bluetoothLeScanner.stopScan(scanCallback)
                Log.i("BluetoothManager", "Stopped scanning...")
            }
            else -> {
                isScanning = true
                bluetoothLeScanner.startScan(scanCallback)
                Log.i("BluetoothManager", "Scanning...")

            }
        }
    }

    companion object{
        const val BEACONDATA = "BEACONDATA"
    }
}
