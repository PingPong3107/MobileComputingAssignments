package com.unistuttgart.taschenrechner

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unistuttgart.taschenrechner.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        val toolbarColour = (binding.toolbar.background as ColorDrawable).color
        window.statusBarColor = toolbarColour

        setupClickListeners()
    }

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

        buttonMap.forEach { (button, value) ->
            button.setOnClickListener { binding.textView.append(value)}
        }

        binding.del.setOnClickListener {
            binding.textView.text = binding.textView.text.dropLast(1).toString()
        }

        binding.del.setOnLongClickListener {
            binding.textView.text = ""
            true
        }

        binding.equals.setOnClickListener {
            val expression = binding.textView.text.toString()
            val expr = ExpressionBuilder(expression).build()
            binding.textView.text = expr.evaluate().toString()
        }
    }
}

