package com.tetsushozawa.paincompass

import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.util.Calendar

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



            val yesterdaySteps =
                intent.getLongExtra("yesterday_steps", 0L)

            val dayBeforeYesterdaySteps =
                intent.getLongExtra("day_before_yesterday_steps", 0L)

            val pressureHpa = intent.getDoubleExtra(
                "pressure_hpa",
                Double.NaN
            )


            val pressureMeasuredAt = intent.getLongExtra(
                "pressure_measured_at",
                0L
            )
            val (pressureChangeHpa, pressureChangeLevel, pressureReferenceAt) =
                calculatePressureChange(pressureHpa, pressureMeasuredAt)
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
                .putLong("latest_yesterday_steps", yesterdaySteps)

                .putLong("latest_day_before_yesterday_steps", dayBeforeYesterdaySteps)

                .putString("latest_memo", memo)
                .putString(
                    "latest_pressure_hpa",
                    if (pressureHpa.isNaN()) "" else pressureHpa.toString()
                )
                .putLong("latest_pressure_measured_at", pressureMeasuredAt)

                .putString(
                    "latest_pressure_change_hpa",
                    pressureChangeHpa?.toString() ?: ""
                )
                .putInt(
                    "latest_pressure_change_level",
                    pressureChangeLevel ?: 99
                )
                .putLong(
                    "latest_pressure_reference_at",
                    pressureReferenceAt ?: 0L
                )
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
                    .put("yesterday_steps", yesterdaySteps)

                    .put("day_before_yesterday_steps", dayBeforeYesterdaySteps)

                    .put(
                        "pressure_hpa",
                        if (pressureHpa.isNaN()) JSONObject.NULL else pressureHpa
                    )
                    .put("pressure_measured_at", pressureMeasuredAt)
                    .put(
                        "pressure_change_hpa",
                        pressureChangeHpa ?: JSONObject.NULL
                    )
                    .put(
                        "pressure_change_level",
                        pressureChangeLevel ?: JSONObject.NULL
                    )
                    .put(
                        "pressure_reference_at",
                        pressureReferenceAt ?: JSONObject.NULL
                    )
            )

            Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show()
            android.util.Log.d(
                "PressureCheck",
                "気圧差=$pressureChangeHpa hPa, 分類=$pressureChangeLevel"
            )
        }

        returnToTopButton.setOnClickListener {
            returnToTop()
        }
    }

    private fun calculatePressureChange(
        pressureHpa: Double,
        pressureMeasuredAt: Long
    ): Triple<Double?, Int?, Long?> {

        if (pressureHpa.isNaN() || pressureMeasuredAt <= 0L) {
            return Triple(null, null, null)
        }

        val repository = PressureRepository(this)
        try {

        // 昨日の0時から今日の0時まで
            val calendar = Calendar.getInstance().apply {
                timeInMillis = pressureMeasuredAt
                set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayStart = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val yesterdayStart = calendar.timeInMillis

        val yesterdayReadings = repository.readingsSince(
            yesterdayStart,
            todayStart - 1
        )

        if (yesterdayReadings.isEmpty()) {
            return Triple(null, null, null)
        }

        // 昨日の測定値のうち、現在時刻に最も近い時刻を選ぶ
        val targetTime = Calendar.getInstance().apply {
            timeInMillis = pressureMeasuredAt
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis

        val previous = yesterdayReadings.minByOrNull {
            kotlin.math.abs(it.timestamp - targetTime)
        } ?: return Triple(null, null, null)

            val difference =
                pressureHpa - previous.pressureHpa.toDouble()

        val level = when {
            difference >= 5.0 -> 2
            difference > 1.0 -> 1
            difference >= -1.0 -> 0
            difference > -5.0 -> -1
            else -> -2
        }

            return Triple(difference, level, previous.timestamp)
        } finally {
            repository.close()
        }
    }
}
