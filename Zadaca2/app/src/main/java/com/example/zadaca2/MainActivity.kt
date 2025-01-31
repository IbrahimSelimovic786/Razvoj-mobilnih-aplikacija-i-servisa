package com.example.zadaca2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var getAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var getHeight: EditText
    private lateinit var getWeight: EditText
    private lateinit var btnCalculate: Button
    private lateinit var btnReset: Button
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getAge = findViewById(R.id.getAge)
        spinnerGender = findViewById(R.id.spinnerGender)
        getHeight = findViewById(R.id.getHeight)
        getWeight = findViewById(R.id.getWeight)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnReset = findViewById(R.id.btnReset)
        tvResult = findViewById(R.id.tvResult)

        btnCalculate.setOnClickListener {
            calculateBMI()
        }

        btnReset.setOnClickListener {
            resetFields()
        }
    }

    private fun calculateBMI() {
        val heightText = getHeight.text.toString()
        val weightText = getWeight.text.toString()

        if (heightText.isNotEmpty() && weightText.isNotEmpty()) {
            val height = heightText.toFloat() / 100
            val weight = weightText.toFloat()
            val bmi = weight / (height * height)

            val category = when {
                bmi < 18.5 -> "Premalo težine"
                bmi < 24.9 -> "Zdravi raspon"
                bmi < 29.9 -> "Prekomjerna težina"
                bmi < 39.9 -> "Gojaznost"
                else -> "Teška gojaznost"
            }

            tvResult.text = "Tvoj BMI: %.2f\nKategorija: %s".format(bmi, category)
        }
    }

    private fun resetFields() {
        getAge.text.clear()
        getHeight.text.clear()
        getWeight.text.clear()
        tvResult.text = "Tvoj BMI: "
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("result_key", tvResult.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val resultText = savedInstanceState.getString("result_key")
        if (resultText != null) {
            tvResult.text = resultText
        }
    }
}
