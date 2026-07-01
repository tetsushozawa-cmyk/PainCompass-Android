package com.example.painrecord001

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class NextActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)

        val weatherSpinner = findViewById<Spinner>(R.id.weatherSpinner)
        val temperatureSpinner = findViewById<Spinner>(R.id.temperatureSpinner)
        val pressureSpinner = findViewById<Spinner>(R.id.pressureSpinner)
        val activitySpinner = findViewById<Spinner>(R.id.activitySpinner)
        val saveButton = findViewById<Button>(R.id.environmentSaveButton)
        val returnToTopButton = findViewById<Button>(R.id.environmentReturnToTopButton)

        setupSpinner(weatherSpinner, R.array.weather_options)
        setupSpinner(temperatureSpinner, R.array.temperature_options)
        setupSpinner(pressureSpinner, R.array.pressure_options)
        setupSpinner(activitySpinner, R.array.daily_activity_options)

        saveButton.setOnClickListener {
            val dateTime = intent.getStringExtra("date_time").orEmpty()
            val painLevel = intent.getStringExtra("pain_level").orEmpty()
            val movement = intent.getStringExtra("movement").orEmpty()
            val sleep = intent.getStringExtra("sleep").orEmpty()
            val brainFog = intent.getStringExtra("brain_fog").orEmpty()
            val painTime = intent.getStringExtra("pain_time").orEmpty()
            val memo = intent.getStringExtra("memo").orEmpty()
            val weather = weatherSpinner.selectedItem.toString()
            val temperature = temperatureSpinner.selectedItem.toString()
            val pressure = pressureSpinner.selectedItem.toString()
            val activity = activitySpinner.selectedItem.toString()

            getSharedPreferences("diary_records", MODE_PRIVATE)
                .edit()
                .putString("latest_date_time", dateTime)
                .putString("latest_pain_level", painLevel)
                .putString("latest_movement", movement)
                .putString("latest_sleep", sleep)
                .putString("latest_brain_fog", brainFog)
                .putString("latest_pain_time", painTime)
                .putString("latest_weather", weather)
                .putString("latest_temperature", temperature)
                .putString("latest_pressure", pressure)
                .putString("latest_daily_activity", activity)
                .putString("latest_memo", memo)
                .apply()

            DiaryRecordStorage.addRecord(
                this,
                JSONObject()
                    .put("date_time", dateTime)
                    .put("pain_level", painLevel)
                    .put("movement", movement)
                    .put("sleep", sleep)
                    .put("brain_fog", brainFog)
                    .put("pain_time", painTime)
                    .put("weather", weather)
                    .put("temperature", temperature)
                    .put("pressure", pressure)
                    .put("daily_activity", activity)
                    .put("memo", memo)
            )

            Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show()
        }

        returnToTopButton.setOnClickListener {
            returnToTop()
        }
    }

    private fun setupSpinner(spinner: Spinner, optionsResId: Int) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            optionsResId,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

}
