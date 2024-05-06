package com.unistuttgart.betterweatherscanner

import android.content.Context
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

        if (item?.contains("F6:B6:2A:79:7B:5D") == true) {
            textView.setTextColor(context.getColor(R.color.red))  // Ensure you have a color defined in your colors.xml
        } else {
            textView.setTextColor(context.getColor(R.color.white)) // Default color
        }

        return view
    }
}
