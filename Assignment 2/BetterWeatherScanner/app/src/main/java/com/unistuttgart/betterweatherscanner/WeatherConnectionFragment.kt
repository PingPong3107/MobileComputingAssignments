package com.unistuttgart.betterweatherscanner

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class WeatherConnectionFragment : Fragment(R.layout.weather_connection_fragment){

    companion object {
        fun newInstance(): WeatherConnectionFragment {
            val fragment = WeatherConnectionFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.weather_connection_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        view.findViewById<Button>(R.id.connectButton).setOnClickListener {
            Toast.makeText(context, "New button clicked", Toast.LENGTH_SHORT).show()
        }
    }

}