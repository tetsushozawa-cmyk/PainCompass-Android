package com.tetsushozawa.paincompass

import android.os.Bundle
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
        var selectedWeather = ""
        var selectedTemperature = ""
        var selectedPressure = ""
        var selectedActivity = ""

        SpinnerHelper.setupSpinner(this, weatherSpinner, R.array.weather_options) {
            selectedWeather = it
        }
        SpinnerHelper.setupSpinner(this, temperatureSpinner, R.array.temperature_options) {
            selectedTemperature = it
        }
        SpinnerHelper.setupSpinner(this, pressureSpinner, R.array.pressure_options) {
            selectedPressure = it
        }
        SpinnerHelper.setupSpinner(this, activitySpinner, R.array.daily_activity_options) {
            selectedActivity = it
        }

        saveButton.setOnClickListener {
            val dateTime = intent.getStringExtra("date_time").orEmpty()
            val painLevel = intent.getStringExtra("pain_level").orEmpty()
            val movement = intent.getStringExtra("movement").orEmpty()
            val sleep = intent.getStringExtra("sleep").orEmpty()
            val brainFog = intent.getStringExtra("brain_fog").orEmpty()
            val painTime = intent.getStringExtra("pain_time").orEmpty()
            val memo = intent.getStringExtra("memo").orEmpty()

            getSharedPreferences("diary_records", MODE_PRIVATE)
                .edit()
                .putString("latest_date_time", dateTime)
                .putString("latest_pain_level", painLevel)
                .putString("latest_movement", movement)
                .putString("latest_sleep", sleep)
                .putString("latest_brain_fog", brainFog)
                .putString("latest_pain_time", painTime)
                .putString("latest_weather", selectedWeather)
                .putString("latest_temperature", selectedTemperature)
                .putString("latest_pressure", selectedPressure)
                .putString("latest_daily_activity", selectedActivity)
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
                    .put("weather", selectedWeather)
                    .put("temperature", selectedTemperature)
                    .put("pressure", selectedPressure)
                    .put("daily_activity", selectedActivity)
                    .put("memo", memo)
            )

            Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show()
        }

        returnToTopButton.setOnClickListener {
            returnToTop()
        }
    }

}
