package com.unistuttgart.taschenrechner

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unistuttgart.taschenrechner.databinding.ActivityMainBinding
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore

/**
 * The main activity of the calculator app
 */
class MainActivity : AppCompatActivity() {

    // View binding for the activity
    private lateinit var binding: ActivityMainBinding

    /**
     * Create the activity and set up the UI
     * @param savedInstanceState the saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set padding to account for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Apply color of toolbar to status bar
        setSupportActionBar(binding.toolbar)
        val toolbarColour = (binding.toolbar.background as ColorDrawable).color
        window.statusBarColor = toolbarColour

        setupClickListeners()
    }

    /**
     * Set up click listeners for all buttons
     * and append the corresponding value to the input field
     */
    private fun setupClickListeners() {
        val buttonMap = mapOf(
            binding.one to getString(R.string._1),
            binding.two to getString(R.string._2),
            binding.three to getString(R.string._3),
            binding.four to getString(R.string._4),
            binding.five to getString(R.string._5),
            binding.six to getString(R.string._6),
            binding.seven to getString(R.string._7),
            binding.eight to getString(R.string._8),
            binding.nine to getString(R.string._9),
            binding.zero to getString(R.string._0),
            binding.plus to getString(R.string.Addition),
            binding.minus to getString(R.string.Subtraction),
            binding.mult to getString(R.string.Multiply),
            binding.div to getString(R.string.Division),
            binding.leftParenthesis to getString(R.string.LeftParenthesis),
            binding.rightParenthesis to getString(R.string.RightParenthesis),
            binding.dot to getString(R.string.Dot)
        )

        // Set up click listeners for all buttons
        buttonMap.forEach { (button, value) ->
            button.setOnClickListener {
                binding.textView.append(value)
                binding.inputScrollView.post {
                    binding.inputScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        // Set up click listener for the delete button
        binding.del.setOnClickListener {
            binding.textView.text = binding.textView.text.dropLast(1).toString()
            binding.inputScrollView.post {
                binding.inputScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        // Set up long click listener for the delete button
        binding.del.setOnLongClickListener {
            binding.textView.text = ""
            true
        }

        // Set up click listener for the equals button
        binding.equals.setOnClickListener {
            val expression = binding.textView.text.toString()
            val calculator = Calculation()
            var result = calculator.calculate(expression)
            if (result.contains(":")) {
                toast(result.split(":")[1])
            } else if(result == "Error in parentheses"){
                toast("Error in parentheses.")
            } else if(result.contains("EmptyStackException")){
                toast("Empty expression provided.")
            }
            else{
                // remove pointless ".0"
                if (result.endsWith(".0")) {
                    result = result.dropLast(2)
                }

                val historyLines = binding.history.text.split("\n").toMutableList()
                if(historyLines.size == 5){
                    historyLines.removeAt(0)
                    binding.history.text = historyLines.joinToString("\n")
                }
                if(binding.history.text.isNotEmpty()){
                    binding.history.append("\n")
                }
                binding.history.append("$expression=$result")
                binding.textView.text = ""
                binding.historyScrollView.post {
                    binding.historyScrollView.fullScroll(View.FOCUS_DOWN)
                }

                easterEggs(expression, result)
            }

        }

        // Set up click listener for the save button
        binding.save.setOnClickListener {
            val history = binding.history.text.toString()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "history.txt")
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                resolver.openOutputStream(uri!!)?.bufferedWriter().use { it?.write(history) }
                toast("History saved to Downloads folder")
            } else {
                toast("This feature is only available on Android 11 and above.")
            }
        }

        // Set up easter egg long click listener for the save button
        binding.save.setOnLongClickListener {
            toast("Matthias Künzer")
            true
        }
    }

    /**
     * Display a toast with the given message
     * @param message the message to display
     */
    private fun toast(message: CharSequence)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    /**
     * Check for easter eggs in the expression and result
     * and display a toast if one is found
     * @param expression the expression to check
     * @param result the result to check
     * @return a toast with the easter egg if one is found
     */
    private fun easterEggs(expression: String, result: String){
        when (result){
            "0" -> toast("Null? NULL!")
            "1" -> toast("Einzzz.")
            "2" -> toast("Swuai.")
            "8" -> toast("Infinity, from a certain point of view.")
            "10" -> toast("Zähn.")
            "13" -> toast("Bad luck.")
            "42" -> toast("The answer to everything!")
            "66" -> toast("It will be done, my lord.")
            "69" -> toast("Nice.")
            "420" -> toast("Blaze it!")
            "666" -> toast("The number of the devil!")
            "1510" -> toast("Fuffzehn Uhr zehn...")
            "1893" -> toast("Ja der VfB!")
            "7353" -> toast("Liest man es auf dem Kopf...")
            "80085" -> toast("Go to horny jail!")
            "91448" -> toast("Traut euch, kommt zu mir!")
        }

        when (expression){
            "007" -> toast("Shaken, not stirred.")
        }

        if (result.toDouble() > 9000 && result.toDouble() < 10000){
            toast("It's over 9000!")
        }
    }
}

