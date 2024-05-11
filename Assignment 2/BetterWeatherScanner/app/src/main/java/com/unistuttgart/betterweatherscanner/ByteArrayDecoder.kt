package com.unistuttgart.betterweatherscanner

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteArrayDecoder {

    fun decodeTemperatureMeasurement(data: ByteArray): Float {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        // Read flags
        val flags = buffer.get()
        val a = buffer.get()
        val b = buffer.get()
        val c = buffer.get()
        val d = buffer.get()

        //val result = Float.fromBits(((a.toInt() shl 23) and 0x7F800000) or ((b.toInt() shl 15) and 0x007F8000) or (((c.toInt() shl 7) and 0x00007F80) or ((d.toInt() shr 1) and 0x0000007F))
        // )

        val hexString =
            byteArrayOf(c, b, a).joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
        val ret = hexString.toInt(16) / 100.0f

        return ret
    }

     fun decodeHumidityMeasurement(data: ByteArray): Int {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        val a = buffer.get()
        val b = buffer.get()
         return ((b.toInt() and 0xFF) shl 8) or (a.toInt() and 0xFF)
    }

    private fun uint16ToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte()
        )
    }
}