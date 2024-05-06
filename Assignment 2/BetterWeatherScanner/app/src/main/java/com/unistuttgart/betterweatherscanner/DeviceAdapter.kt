package com.unistuttgart.betterweatherscanner

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class DeviceAdapter(context: Context, private val resource: Int, private val items: List<String>)
    : ArrayAdapter<String>(context, resource, items) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(resource, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        val item = getItem(position)
        textView.text = item


        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK


        if (item?.contains("IPVS") == true){
            textView.setTextColor(context.getColor(R.color.red))  // Ensure you have a color defined in your colors.xml
        } else {
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                // Night mode is active, we're using dark theme
                textView.setTextColor(context.getColor(R.color.white))
            } else {
                // Not night mode, we're using day/light theme
                textView.setTextColor(context.getColor(R.color.black))
            }
        }

        return view
    }
}
