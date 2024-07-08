package de.uni_s.ipvs.mcl.assignment5

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * This class represents the main activity of the application.
 *
 * The main activity fetches the cities from the Firebase database and updates the current and average temperature.
 * The user can add a temperature to a city and subscribe to a city.
 * The user can see the current and average temperature of a city and the time of the last update.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var team6ref: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var adapter: CityTemperatureAdapter
    private lateinit var cityList: ObservableMutableList<City>

    /**
     * This function is called when the activity is created.
     *
     * In this function, the database and the city adapter are initialized.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.app_name)
        toolbar.setTitleTextColor(Color.BLACK)

        insertTemperatureButton()

        cityList = ObservableMutableList()
        listView = findViewById(R.id.listview)
        database = Firebase.database
        team6ref = database.getReference("teams").child("6")
        fetchCities()

        adapter = CityTemperatureAdapter(this, R.layout.list_item_city, cityList)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val city = adapter.getItem(position)
            city?.let {
                city.setSubscribed(!city.isSubscribed())
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Sets a click listener on the insert button to add a temperature to a city.
     *
     * The city name and temperature are read from the input fields.
     * The temperature is added to the city in the Firebase database.
     * A toast message is shown to the user to indicate success or failure.
     * Illegal input is detected and a toast message is shown to the user.
     */
    private fun insertTemperatureButton() {
        val button = findViewById<Button>(R.id.insertButton)
        button.setOnClickListener {
            val city = findViewById<EditText>(R.id.cityInput).text.toString()
            val tempStr = findViewById<EditText>(R.id.tempInput).text.toString()
            if (city != "" && tempStr != "" && city.matches("[a-zA-ZäöüÄÖÜß -]*".toRegex())) {
                val temperature = tempStr.toDouble()
                addTemperatureToCity(city, temperature)
                Toast.makeText(this, String.format(getString(R.string.tempAddedToCity), city), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.tempAddedToCityErr), Toast.LENGTH_SHORT).show()
            }

        }
    }

    /**
     * This function adds a temperature to a city in the Firebase database.
     *
     * The temperature is added to the city for the current date.
     * @param city The name of the city to add the temperature to.
     * @param temperature The temperature to add to the city.
     * @see fetchCities
     */
    private fun addTemperatureToCity(city: String, temperature: Double) {
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

    /**
     * This function fetches the cities from the Firebase database.
     *
     * The cities are fetched from the Firebase database and the current temperature and average temperature are updated.
     * The cities are added to the cityList and the adapter is notified of the changes.
     * @see handleCitySnapshot
     */
    private fun fetchCities() {
        val citiesRef = team6ref.child("location")
        citiesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                handleCitySnapshot(dataSnapshot)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                handleCitySnapshot(dataSnapshot)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.i("MainActivity", "City removal not implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.i("MainActivity", "City move not implemented")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MainActivity", "Database error occurred: $databaseError")
            }
        })
    }

    /**
     * This function handles a snapshot of a city from the Firebase database.
     *
     * The latest temperature entry for the city is updated.
     * The average temperature for the city is updated.
     * @param dataSnapshot The snapshot of the city from the Firebase database.
     * @see updateOrAddCityTemperature
     * @see updateAverageTemperature
     */
    private fun handleCitySnapshot(dataSnapshot: DataSnapshot) {
        val cityName = dataSnapshot.key
        val currentDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).toString()
        cityName?.let {
            val latestDateSnapshot = dataSnapshot.children.lastOrNull()
            latestDateSnapshot?.let { dateSnapshot ->
                if (dateSnapshot.key != currentDate) {
                    return
                }
                updateOrAddCityTemperature(cityName, dateSnapshot)
                updateAverageTemperature(cityName, dateSnapshot)
            }
        }
    }

    /**
     * This function updates the latest temperature entry for a city.
     *
     * The latest temperature entry for the city is updated.
     * If the city is not in the cityList, it is added.
     * The adapter is notified of the changes.
     * @param cityName The name of the city to update.
     * @param dateSnapshot The snapshot of the latest temperature entries for the city.
     * @see City
     */
    private fun updateOrAddCityTemperature(cityName: String, dateSnapshot: DataSnapshot) {
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
                Toast.makeText(this, getString(R.string.parsingError), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * This function updates the average temperature for a city.
     *
     * The average temperature for the city is updated.
     * The city is found in the cityList and the average temperature is set.
     * The adapter is notified of the changes.
     * @param cityName The name of the city to update.
     * @param dateSnapshot The snapshot of the latest temperature entries for the city.
     * @see City
     */
    private fun updateAverageTemperature(cityName: String, dateSnapshot: DataSnapshot) {
        val temperatures = dateSnapshot.children.mapNotNull { entrySnapshot ->
            val entry = entrySnapshot.value.toString()
            try {
                val (_, temperature) = entry.split(":")
                temperature.toDouble()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error parsing temperature", e)
                null
            }
        }
        val averageTemperature = temperatures.average()
        val city = cityList.find { it.getCityName() == cityName }
        city?.setAverageTemperature(averageTemperature.toString())
        adapter.notifyDataSetChanged()
    }

    /**
     * This function resets the Firebase database.
     */
    private fun resetDatabase() {
        team6ref.child("location").removeValue().addOnCompleteListener {
            Log.d("MainActivity", "All Data deleted successfully")
        }
    }
}