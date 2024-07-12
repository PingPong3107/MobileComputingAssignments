package de.uni_s.ipvs.mcl.assignment5

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    private lateinit var citiesRef: DatabaseReference

    private val childEventListener = object : ChildEventListener {
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
    }

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
        toolbar.title = String.format(getString(R.string.mode), "Test DB")
        setSupportActionBar(toolbar)

        toolbar.setTitleTextColor(Color.BLACK)

        insertTemperatureButton()

        cityList = ObservableMutableList()
        listView = findViewById(R.id.listview)
        database = Firebase.database
        team6ref = database.getReference("teams").child("6")
        fetchCities()
        inputFieldNavigation()

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
     * This function is called when the options menu is created.
     * The options menu is inflated with the menu_main.xml file.
     * @param menu The options menu to create.
     * @return true if the menu is created successfully, false otherwise.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * This function is called when an item in the options menu is selected.
     *
     * The function changes the Firebase database to the production or test database.
     * The cities are fetched from the new database.
     * @param item The item in the options menu that was selected.
     * @return true if the item is selected successfully, false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        return when (item.itemId) {
            R.id.use_prod_database -> {
                toolbar.title = String.format(getString(R.string.mode), "Prod DB")
                team6ref = database.getReference()
                cityList.clear()
                resetListener()
                fetchCities()
                true
            }
            R.id.use_test_database -> {
                toolbar.title = String.format(getString(R.string.mode), "Test DB")
                team6ref = database.getReference("teams").child("6")
                cityList.clear()
                resetListener()
                fetchCities()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * This function assigns editor action listeners to the input fields.
     *
     * The editor action listener for the city input field is assigned to focus the temperature input field.
     * The editor action listener for the temperature input field is assigned to add the temperature to the city.
     * The keyboard is closed after the temperature is added.
     * @see closeKeyboard
     * @see checkInputAndAddCity
     */
    private fun inputFieldNavigation(){
        val cityInput = findViewById<EditText>(R.id.cityInput)
        val tempInput = findViewById<EditText>(R.id.tempInput)

        cityInput.setOnEditorActionListener{ _, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_NEXT ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER){
                tempInput.requestFocus()
                true
            } else {
                false
            }
        }

        tempInput.setOnEditorActionListener{ _, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER){
                val city = cityInput.text.toString()
                val temp = tempInput.text.toString()
                checkInputAndAddCity(city, temp)
                cityInput.text.clear()
                tempInput.text.clear()
                closeKeyboard()
                true
            } else {
                false
            }
        }
    }

    /**
     * This function closes the keyboard.
     */
    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
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
            val cityInput = findViewById<EditText>(R.id.cityInput)
            val tempInput = findViewById<EditText>(R.id.tempInput)
            checkInputAndAddCity(cityInput.text.toString(), tempInput.text.toString())
            cityInput.text.clear()
            tempInput.text.clear()
            closeKeyboard()
        }
    }

    /**
     * This function checks the city and temperature input and adds the temperature to a city object.
     *
     * @param city The name of the city
     * @param temp The temperature of the corresponding city
     * @see addTemperatureToCity
     */
    private fun checkInputAndAddCity(city: String, temp: String){
        if (city != "" && temp != "" && temp != "." && city.validateCityName()) {
            val temperature = temp.toDouble()

            // Trim and capitalize city name
            val cityName = city.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            addTemperatureToCity(cityName, temperature)
            Toast.makeText(this, String.format(getString(R.string.tempAddedToCity), city), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.tempAddedToCityErr), Toast.LENGTH_SHORT).show()
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

        tempRef.child(time).setValue(temperature).addOnCompleteListener { task ->
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
        citiesRef = team6ref.child("location")
        citiesRef.addChildEventListener(childEventListener)
    }

    /**
     * This function resets the Firebase database listener.
     */
    private fun resetListener() {
        citiesRef.removeEventListener(childEventListener)
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
        cityName?.let {
            val latestDateSnapshot = dataSnapshot.children.lastOrNull()
            latestDateSnapshot?.let { dateSnapshot ->
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
            try {
                val time = entrySnapshot.key.toString()
                if (time.isValidTimestamp().not()) {
                    Log.e("MainActivity", "Invalid timestamp")
                    return
                }
                val temperature = entrySnapshot.value.toString()
                if (temperature.isValidTemperature().not()) {
                    Log.e("MainActivity", "Invalid temperature")
                    Toast.makeText(this, "Error: $cityName, tmp='$temperature'", Toast.LENGTH_SHORT).show()
                    return
                }
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
            val temperature = entrySnapshot.value.toString()
            if (temperature.isValidTemperature()) {
                temperature.toDouble()
            } else {
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
    @Suppress("unused")
    private fun resetDatabase() {
        team6ref.child("location").removeValue().addOnCompleteListener {
            Log.d("MainActivity", "All Data deleted successfully")
        }
    }

    /**
     * This function validates if the string is a properly formatted city name
     */
    private fun String.validateCityName() = all {
        it.isLetter() || it == '-' || it == ' '
    }

    /**
     * This function validates if the string is a properly formatted timestamp
     */
    private fun String.isValidTimestamp() = try {
        val ts = toLong()
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
            formatter.format(Date(ts))
        }
        else{
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(ts))
        }
        true
    } catch (e: NumberFormatException) {
        false
    }

    /**
     * This function validates if the string is a properly formatted temperature
     */
    private fun String.isValidTemperature() = try {
        toDouble()
        true
    } catch (e: NumberFormatException) {
        false
    }
}