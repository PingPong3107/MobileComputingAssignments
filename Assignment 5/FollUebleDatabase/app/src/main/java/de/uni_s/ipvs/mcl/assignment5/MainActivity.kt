package de.uni_s.ipvs.mcl.assignment5

import CityTemperatureAdapter
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {



    private lateinit var database:FirebaseDatabase
    private lateinit var team6ref: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var adapter: CityTemperatureAdapter
    private lateinit var cityList: ObservableMutableList<City>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        insertTemperatureButton()

        cityList = ObservableMutableList()
        listView = findViewById(R.id.listview)
        database = Firebase.database
        team6ref = database.getReference("teams").child("6")
        //resetDatabase()
        fetchCities()

        adapter = CityTemperatureAdapter(this, R.layout.list_item_city, cityList)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            val city = adapter.getItem(position)
            city?.let {
                city.setSubscribed(!city.isSubscribed())
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun addTemperatureToCity(city: String, temperature: Double){

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val time = System.currentTimeMillis().toString()

        val tempRef = team6ref.child("location").child(city).child(date)

        val string = "$time:$temperature"

        tempRef.push().setValue(string).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainActivity", "Temperature data added successfully")
            } else {
                Log.e("MainActivity", "Error adding temperature data", task.exception)
            }
        }
    }

    private fun resetDatabase(){
        team6ref.child("location").removeValue().addOnCompleteListener {
            Log.d("MainActivity", "Alle Daten gelöscht")
        }
    }

    private fun insertTemperatureButton(){
        val button = findViewById<Button>(R.id.insertButton)
        button.setOnClickListener {
            val city = findViewById<EditText>(R.id.cityInput).text.toString()
            val tempStr = findViewById<EditText>(R.id.tempInput).text.toString()
            if (city != "" && tempStr != "" && city.matches("[a-zA-ZäöüÄÖÜ -]*".toRegex())){
                val temperature = tempStr.toDouble()
                addTemperatureToCity(city, temperature)
                Toast.makeText(this, "Temperatur für $city hinzugefügt", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Illegal input", Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun fetchCities() {
        val citiesRef = team6ref.child("location")
        citiesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                //val cityName = dataSnapshot.key
                //cityName?.let {
                //    fetchLatestTemperature(City(cityName))
                //}
                handleCitySnapshot(dataSnapshot)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                //val cityName = dataSnapshot.key
                //cityName?.let {
                //    for(c in cityList){
                //        if (cityName == c.getCityName()){
                //            fetchLatestTemperature(c)
                //        }
                //    }


                //}
                handleCitySnapshot(dataSnapshot)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                //braucht keyn Mensch
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //braucht keyn Mensch
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MainActivity","Äbbas isch henich: $databaseError")
            }
        })
    }

    private fun handleCitySnapshot(dataSnapshot: DataSnapshot) {
        val cityName = dataSnapshot.key
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).toString()
        cityName?.let {
            val latestDateSnapshot = dataSnapshot.children.lastOrNull()
            latestDateSnapshot?.let { dateSnapshot ->
                if (dateSnapshot.key != currentDate){
                    return
                }
                val latestEntrySnapshot = dateSnapshot.children.lastOrNull()
                latestEntrySnapshot?.let { entrySnapshot ->
                    val latestEntry = entrySnapshot.value.toString()
                    try {
                        val (time, temperature) = latestEntry.split(":")
                        val city = cityList.find { it.getCityName() == cityName } ?: City(cityName)
                        city.setCurrentTemperature(temperature)
                        city.setUpdateTime(time)
                        if (!cityList.contains(city)) {
                            cityList.add(city)
                        }
                        adapter.notifyDataSetChanged()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error parsing latest entry", e)
                        Toast.makeText(this, "Error parsing latest entry", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun fetchLatestTemperature(city: City) {
        val cityRef = team6ref.child("location").child(city.getCityName())
        cityRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latestDateSnapshot = snapshot.children.first()
                val latestDate = latestDateSnapshot.key
                if (latestDate != SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).toString()){
                    return
                }
                latestDateSnapshot.ref.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val (time, temperature) = snapshot.children.first().value.toString().split(":")
                            city.setCurrentTemperature(temperature)
                            city.setUpdateTime(time)
                            if(!cityList.contains(city)){
                                cityList.add(city)
                            }
                            adapter.notifyDataSetChanged()

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



