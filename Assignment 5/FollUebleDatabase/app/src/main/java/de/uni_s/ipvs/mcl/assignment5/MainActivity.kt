package de.uni_s.ipvs.mcl.assignment5

import CityTemperatureAdapter
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
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


    private lateinit var newCityTemperatureMap: ObservableMutableList<City>


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

        newCityTemperatureMap = ObservableMutableList()
        listView = findViewById(R.id.listview)
        database = Firebase.database
        team6ref = database.getReference("teams").child("6")
        //insertTemperatureData("Stuttgart","2011-2-2", System.currentTimeMillis().toString(), 26.0)
//        deleteTestingStuff()


        //addTemperatureToCity("Horra", -6.0)


        //getDataFromFirebase()
        fetchCities()

        adapter = CityTemperatureAdapter(this, R.layout.list_item_city, newCityTemperatureMap)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            val city = adapter.getItem(position)
            city?.let {
                city.setSubscribed(!city.isSubscribed())
                adapter.notifyDataSetChanged()
            }
        }


        /**
        resetDatabase()
        addTemperatureToCity("Gündelbach", 46.0)

        Handler(Looper.getMainLooper()).postDelayed({
            addTemperatureToCity("Gündelbach", 26.0)
            getDataFromFirebase()
        }, 5000)

        */





    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun String.toHumanReadableTime(): String {
        val millis = this.toLongOrNull() ?: return "Invalid milliseconds"
        return DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(millis))
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

    @SuppressLint("NewApi")
    private fun getDataFromFirebase(){
        team6ref.child("location").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                //Log.d("MainActivity", "Data exists")
                for (citySnapshot in dataSnapshot.children) {
                    val city = citySnapshot.key
                    val cityObj = City(city.toString())
                    newCityTemperatureMap.add(cityObj)
                    //Log.d("MainActivity", "City: $city")
                    for (dateSnapshot in citySnapshot.children) {
                        val date = dateSnapshot.key
                        //Log.d("MainActivity", "Date: $date")
                        for (timeSnapshot in dateSnapshot.children) {
                            var (time, temperature) = timeSnapshot.value.toString().split(":")
                            cityObj.setCurrentTemperature(temperature)
                            cityObj.setUpdateTime(time)

                            //Log.d("MainActivity", "Time: ${time.toHumanReadableTime()}, Temperature: $temperature")
                        }
                    }
                }
            } else {
                Log.e("MainActivity", "No data found")
            }
        }.addOnFailureListener {
            Log.e("MainActivity", "Error getting data", it)
        }
    }

    private fun resetDatabase(){
        team6ref.child("location").removeValue().addOnCompleteListener {
            Log.d("MainActivity", "Alle Daten gelöscht")
        }
    }

    private fun insertTemperatureButton(){
        val button = findViewById<Button>(R.id.insertButton)
        button.setOnClickListener {val city = findViewById<EditText>(R.id.cityInput).text.toString()
            val temperature = findViewById<EditText>(R.id.tempInput).text.toString().toDouble()
            addTemperatureToCity(city, temperature)
            Toast.makeText(this, "Temperatur für $city hinzugefügt", Toast.LENGTH_SHORT).show()

        }
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



        citiesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This will be called for each child node initially and for new child nodes
                // when they are added
                val cityName = dataSnapshot.key
                cityName?.let {
                    val city: City = City(cityName)
                    fetchLatestTemperature(city)
                }
                // Do something with the object
            }

            override fun onChildChanged(
                dataSnapshot: DataSnapshot,
                previousChildName: String?
            ) {
                println("data changeDDDDDDDDD")
                val cityName = dataSnapshot.key
                cityName?.let {
                    for(c in newCityTemperatureMap){
                        if (cityName == c.getCityName()){
                            fetchLatestTemperature(c)
                        }
                    }

                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // This will be called when a child node is removed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //braucht keyn Mensch
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MainActivity","Äbbas isch henich: $databaseError")
            }
        })




            /**
            task ->
            if (task.isSuccessful) {
                newCityTemperatureMap.clear()

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
            }*/
        }


    private fun fetchLatestTemperature(city: City) {
        val cityRef = team6ref.child("location").child(city.getCityName())
        cityRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latestDateSnapshot = snapshot.children.first()
                latestDateSnapshot.ref.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            println("debug alda "+snapshot.children.first().value.toString())
//                            val latestTemperature = snapshot.children.first().value as Double
                            var (time, temperature) = snapshot.children.first().value.toString().split(":")
                            city.setCurrentTemperature(temperature)
                            city.setUpdateTime(time)
                            if(!newCityTemperatureMap.contains(city)){
                                newCityTemperatureMap.add(city)
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



