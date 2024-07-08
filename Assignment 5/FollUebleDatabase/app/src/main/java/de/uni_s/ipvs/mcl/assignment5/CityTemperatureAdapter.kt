package de.uni_s.ipvs.mcl.assignment5

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * This class represents an adapter for the city list.
 *
 * @property context The context of the adapter.
 * @property resource The resource of the adapter.
 * @property cityList The list of cities.
 */
class CityTemperatureAdapter(context: Context, resource: Int, private val cityList: ObservableMutableList<City>) : ArrayAdapter<City>(context, resource, cityList) {

    private var res: Int = 0

    /**
     * This function returns the view of the city.
     */
    init {
        cityList.addObserver { notifyDataSetChanged() }
        res = resource
    }

    /**
     * This function returns the view of one city.
     * @param position The position of the city.
     * @param convertView The view of the city.
     * @param parent The parent of the view.
     * @return The view of the city.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(res, parent, false)
        val city = cityList[position]

        val cityName = city.getCityName()
        val temperature = city.getCurrentTemperature()
        val time = city.getUpdateTime()
        val avgTemp = city.getAverageTemperature()

        val text1 = view.findViewById<View>(android.R.id.text1) as TextView
        val text2 = view.findViewById<View>(android.R.id.text2) as TextView

        text1.text = cityName
        text2.text = "Current: $temperature, Avg: $avgTemp,\nChanged: $time"
        if (city.isSubscribed()) {
            text2.visibility = View.VISIBLE;
        } else {
            text2.visibility = View.GONE;
        }

        return view
    }
}