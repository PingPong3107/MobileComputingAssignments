package com.unistuttgart.taschenrechner

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unistuttgart.taschenrechner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        setSupportActionBar(binding.toolbar)

        val colorDrawable = binding.toolbar.background as ColorDrawable
        val color = colorDrawable.color

        window.statusBarColor = color

        binding.button1.setOnClickListener {
            binding.textView.append("7")
        }
        binding.button2.setOnClickListener {
            binding.textView.append("8")
        }
        binding.button3.setOnClickListener {
            binding.textView.append("9")
        }
        binding.button4.setOnClickListener {
            binding.textView.text=binding.textView.text.dropLast(1).toString()
        }
        binding.button5.setOnClickListener {
            binding.textView.append("4")
        }
        binding.button6.setOnClickListener {
            binding.textView.append("5")
        }
        binding.button7.setOnClickListener {
            binding.textView.append("6")
        }
        binding.button8.setOnClickListener {
            binding.textView.append("+")
        }
        binding.button9.setOnClickListener {
            binding.textView.append("1")
        }
        binding.button10.setOnClickListener {
            binding.textView.append("2")
        }
        binding.button11.setOnClickListener {
            binding.textView.append("3")
        }
        binding.button12.setOnClickListener {
            binding.textView.append("-")
        }
        binding.button13.setOnClickListener {
            binding.textView.append("0")
        }
        binding.button14.setOnClickListener {
            binding.textView.append(".")
        }
        binding.button15.setOnClickListener { }
        binding.button16.setOnClickListener {
            binding.textView.append("*")
        }
        binding.button17.setOnClickListener {
            binding.textView.append("(")
        }
        binding.button18.setOnClickListener {
            binding.textView.append(")")
        }
        binding.button19.setOnClickListener { }
        binding.button20.setOnClickListener {
            binding.textView.append("/")
        }

    }
}