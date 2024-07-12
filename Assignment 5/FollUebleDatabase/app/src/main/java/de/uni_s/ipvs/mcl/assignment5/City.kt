package de.uni_s.ipvs.mcl.assignment5

import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.round

/**
 * This Class represents a city with its current and average temperature.
 *
 * @property name The name of the city.
 * @property currentTemperature The current temperature of the city.
 * @property averageTemperature The average temperature of the city.
 * @property updateTime The time when the temperature was last updated.
 * @property subscribed A flag indicating whether the city is subscribed or not.
 */
class City(private var name: String){
    private var currentTemperature: Double? = null
    private var averageTemperature: Double? = null
    private var updateTime: String? = null
    private var subscribed: Boolean = false

    /**
     * This function returns the name of the city.
     */
    fun getCityName(): String {
        return name
    }

    /**
     * This function returns the current temperature of the city.
     */
    fun getCurrentTemperature(): String {
        if (currentTemperature == null) {
            return "N/A"
        }
        return "${currentTemperature}\u2103"
    }

    /**
     * This function returns the average temperature of the city.
     */
    fun getAverageTemperature(): String {
        if (averageTemperature == null) {
            return "N/A"
        }
        return "${averageTemperature}\u2103"
    }

    /**
     * This function returns the time when the temperature was last updated.
     */
    fun getUpdateTime(): String {
        if (updateTime == "") {
            return "N/A"
        }
        return updateTime?.toHumanReadableTime() ?: "N/A"
    }

    /**
     * This function returns the subscription status of the city.
     */
    fun isSubscribed(): Boolean {
        return subscribed
    }

    /**
     * This function sets the current temperature of the city.
     *
     * @param temperature The current temperature of the city.
     */
    fun setCurrentTemperature(temperature: String) {
        currentTemperature = try {
            round(temperature.toDouble()*100)/100
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * This function sets the average temperature of the city.
     *
     * @param temperature The average temperature of the city.
     */
    fun setAverageTemperature(temperature: String) {
        averageTemperature = try {
            round(temperature.toDouble()*100)/100
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * This function sets the time when the temperature was last updated.
     *
     * @param time The time when the temperature was last updated.
     */
    fun setUpdateTime(time: String) {
        updateTime = time
    }

    /**
     * This function sets the subscription status of the city.
     *
     * @param sub The subscription status of the city.
     */
    fun setSubscribed(sub: Boolean) {
        subscribed = sub
    }

    /**
     * This function returns a string representation of the city.
     */
    override fun toString(): String {
        return getCityName()
    }

    /**
     * This function converts a string representing milliseconds to a human readable time.
     */
    private fun String.toHumanReadableTime(): String? {
        val millis = this.toLongOrNull() ?: return null
        val pattern = "yyyy-MM-dd HH:mm:ss.SSS"
        return try {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                val formatter = SimpleDateFormat(pattern, Locale.getDefault())
                formatter.format(Date(millis))
            } else {
                DateTimeFormatter.ofPattern(pattern)
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(millis))
            }
        } catch (e: Exception) {
            Log.e("City", "Error parsing time", e)
            null
        }
    }

    /**
     * This function checks if two cities are equal.
     *
     * @param other The other city to compare.
     */
    override fun equals(other: Any?): Boolean {
        when(other){
            is City -> return this.getCityName() == other.getCityName()
        }
        return false
    }

    /**
     * This function returns the hash code of the city.
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (currentTemperature?.hashCode() ?: 0)
        result = 31 * result + (averageTemperature?.hashCode() ?: 0)
        result = 31 * result + (updateTime?.hashCode() ?: 0)
        result = 31 * result + subscribed.hashCode()
        return result
    }
}