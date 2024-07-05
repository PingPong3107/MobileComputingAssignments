import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import de.uni_s.ipvs.mcl.assignment5.City
import de.uni_s.ipvs.mcl.assignment5.ObservableMutableList

class CityTemperatureAdapter(context: Context, resource: Int, private val cityList: ObservableMutableList<City>) : ArrayAdapter<City>(context, resource, cityList) {

    var res: Int = 0

    init {
        cityList.forEach { city ->
            city.currentTemperature.observe(context as LifecycleOwner) { notifyDataSetChanged() }
            city.averageTemperature.observe(context as LifecycleOwner) { notifyDataSetChanged() }
            city.updateTime.observe(context as LifecycleOwner) { notifyDataSetChanged() }
            city.subscribed.observe(context as LifecycleOwner) { notifyDataSetChanged()}
        }
        cityList.addObserver { notifyDataSetChanged() }
        res = resource
    }
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
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
        if (city.subscribed.value == true) {
            text2.visibility = View.VISIBLE;
        } else {
            text2.visibility = View.GONE;
        }

        return view
    }
}