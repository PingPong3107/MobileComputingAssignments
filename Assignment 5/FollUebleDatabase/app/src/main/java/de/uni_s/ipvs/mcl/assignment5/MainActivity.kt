package de.uni_s.ipvs.mcl.assignment5

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {



    private lateinit var database:FirebaseDatabase
    private lateinit var team6ref: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var cityList: MutableList<String>
    private lateinit var cityTemperatureMap: MutableMap<String, Double>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cityList = mutableListOf()
        cityTemperatureMap = mutableMapOf()
        listView = findViewById(R.id.listview)
        database = Firebase.database
        team6ref = database.getReference("teams").child("6")
        insertTemperatureData("Stuttgart","2011-2-2", System.currentTimeMillis().toString(), 26.0)
//        deleteTestingStuff()
        fetchCities()


    }

    private fun insertTemperatureData(city: String, date: String, time: String, temperature: Double) {
        team6ref.child("location").child(city).child(date).child(time).setValue(temperature)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Temperaturdaten für $city am $date um $time erfolgreich eingefügt: $temperature")
                } else {
                    println("Fehler beim Einfügen der Temperaturdaten: ${task.exception}")
                }
            }
    }

    private fun readTemperatureData(city: String, date: String, time: String) {
        team6ref.child("location").child(city).child(date).child(time).get().addOnSuccessListener {
            val value = it.value
            println("Temperaturdaten für $city am $date um $time: $value")
        }.addOnFailureListener {
            println("Fehler beim Lesen der Temperaturdaten: ${it.message}")
        }
    }


    private fun deleteTestingStuff(){
        team6ref.child("location").removeValue().addOnCompleteListener {
            println("Alle Daten gelöscht")
        }
    }

    private fun fetchCities() {
        val citiesRef = team6ref.child("location")
        // Display the cities in the ListView with temperature
        val adapter = object : ArrayAdapter<String>(this@MainActivity, R.layout.list_item_city, cityList) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                val cityName = cityList[position]
                val temperature = cityTemperatureMap[cityName]

                // Append temperature to city name in list item
                val text = "$cityName - Temperature: ${temperature ?: "N/A"}"
                (view as android.widget.TextView).text = text

                return view
            }
        }
        listView.adapter = adapter

        citiesRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cityList.clear()
                cityTemperatureMap.clear()

                val dataSnapshot = task.result
                for (citySnapshot in dataSnapshot.children) {
                    val cityName = citySnapshot.key
                    cityName?.let {
                        cityList.add(it)
                        fetchLatestTemperature(it)
                    }
                }


            } else {
                Log.e("MainActivity", "Error getting data", task.exception)
            }
        }
    }

    private fun fetchLatestTemperature(cityName: String) {
        val cityRef = team6ref.child("location").child(cityName)
        cityRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latestDateSnapshot = snapshot.children.first()
                latestDateSnapshot.ref.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            println("debug alda "+snapshot.children.first().value.toString())
//                            val latestTemperature = snapshot.children.first().value as Double
                            cityTemperatureMap[cityName] = 0.0
                            listView.adapter?.let {
                                (it as ArrayAdapter<*>).notifyDataSetChanged()
                            }
                        } else {
                            Log.e("MainActivity", "No data for latest time")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MainActivity", "Error getting data for latest time", error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error getting data for latest date", error.toException())
            }
        })
    }


}



