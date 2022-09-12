package com.playground.kspplayground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.playground.kspplayground.databinding.ActivityMainBinding
import com.playground.processor.generated.PlaygroundGenerated

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.text.text =
            "AppCompatActivity classes: \n${PlaygroundGenerated.validatedClass.joinToString(",\n")}"
    }
}