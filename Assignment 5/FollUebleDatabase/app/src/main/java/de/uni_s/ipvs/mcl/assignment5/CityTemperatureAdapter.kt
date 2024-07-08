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
 * @param context The context of the adapter.
 * @param resource The resource of the adapter.
 * @property cityList The list of cities.
 */
class CityTemperatureAdapter(context: Context, resource: Int, private val cityList: ObservableMutableList<City>) : ArrayAdapter<City>(context, resource, cityList) {

    private var res: Int = 0

    /**
     * This function returns the view of the city after an observer on the city data was added.
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

        val cityNameView = view.findViewById<View>(android.R.id.text1) as TextView
        val cityInfoView = view.findViewById<View>(android.R.id.text2) as TextView

        cityNameView.text = city.getCityName()
        cityInfoView.text = String.format(context.getString(R.string.cityInfo),
            city.getCurrentTemperature(),
            city.getAverageTemperature(),
            city.getUpdateTime())

        if (city.isSubscribed()) {
            cityInfoView.visibility = View.VISIBLE
        } else {
            cityInfoView.visibility = View.GONE
        }
        return view
    }
}