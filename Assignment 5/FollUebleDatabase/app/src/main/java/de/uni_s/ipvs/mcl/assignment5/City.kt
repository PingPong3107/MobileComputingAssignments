package de.uni_s.ipvs.mcl.assignment5

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.reflect.typeOf

class City(private var name: String){

    private val _currentTemperature = MutableLiveData<Double>()
    val currentTemperature: MutableLiveData<Double> get() = _currentTemperature

    private val _averageTemperature = MutableLiveData<Double>()
    val averageTemperature: MutableLiveData<Double> get() = _averageTemperature

    private val _updateTime = MutableLiveData<String>()
    val updateTime: MutableLiveData<String> get() = _updateTime

    private val _subscribed = MutableLiveData<Boolean>(false)
    val subscribed: MutableLiveData<Boolean> get() = _subscribed
    @RequiresApi(Build.VERSION_CODES.O)
    fun String.toHumanReadableTime(): String {
        val millis = this.toLongOrNull() ?: return "Invalid milliseconds"
        return DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(millis))
    }

    fun getCityName(): String {
        return name
    }

    fun getCurrentTemperature(): Double? {
        return _currentTemperature.value
    }

    fun getAverageTemperature(): Double? {
        return _averageTemperature.value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUpdateTime(): String {
        if (_updateTime.value == "") {
            return "N/A"
        }
        return _updateTime.value?.toHumanReadableTime() ?: "N/A"
    }

    fun setCurrentTemperature(temperature: String) {
        val currentTemp = try {
            temperature.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
        _currentTemperature.value = currentTemp
    }

    fun setAverageTemperature(temperature: String) {
        val averageTemp = try {
            temperature.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
        _averageTemperature.value = averageTemp

    }

    fun setUpdateTime(time: String) {
        _updateTime.value = time
    }

    fun setSubscribed(subscribed: Boolean) {
        Log.d("City", "setSubscribed: $subscribed")
        _subscribed.value = subscribed
        Log.d("City", "setSubscribed: ${_subscribed.value}")
    }

    fun isSubscribed(): Boolean {
        return _subscribed.value ?: false
    }

    override fun toString(): String {
        return getCityName()
    }

    override fun equals(other: Any?): Boolean {
        when(other){
            is City -> return this.getCityName() == other.getCityName()
        }
        return false
    }

}