import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import de.uni_s.ipvs.mcl.assignment5.City
import de.uni_s.ipvs.mcl.assignment5.ObservableMutableList

class CityTemperatureAdapter(context: Context, resource: Int, private val cityList: ObservableMutableList<City>) : ArrayAdapter<City>(context, resource, cityList) {

    init {
        cityList.forEach { city ->
            city.currentTemperature.observe(context as LifecycleOwner) { notifyDataSetChanged() }
            city.averageTemperature.observe(context as LifecycleOwner) { notifyDataSetChanged() }
            city.updateTime.observe(context as LifecycleOwner) { notifyDataSetChanged() }
            city.subscribed.observe(context as LifecycleOwner) { notifyDataSetChanged()}
        }
        cityList.addObserver { notifyDataSetChanged() }
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val city = cityList[position]

        val cityName = city.getCityName()
        val temperature = city.getCurrentTemperature()

        // Append temperature to city name in list item
        val text = "$cityName - Temperature: ${temperature ?: "N/A"}"
        if (city.subscribed.value == true) {
            (view as TextView).text = text
        } else {
            (view as TextView).text = cityName
        }
        //(view as TextView).text = text


        return view
    }
}