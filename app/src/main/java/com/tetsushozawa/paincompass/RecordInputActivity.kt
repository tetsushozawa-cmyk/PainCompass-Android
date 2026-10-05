package com.tetsushozawa.paincompass

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import java.time.ZonedDateTime
import kotlinx.coroutines.launch
class RecordInputActivity : AppCompatActivity() {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_input)

        val dateTimeText = findViewById<TextView>(R.id.dateTimeText)
        val yesterdayStepsText = findViewById<TextView>(R.id.yesterdayStepsText)
        val dayBeforeYesterdayStepsText = findViewById<TextView>(R.id.dayBeforeYesterdayStepsText)

        val memoEditText = findViewById<EditText>(R.id.dateScreenMemoEditText)
        val painLevelSpinner = findViewById<Spinner>(R.id.painLevelSpinner)
        val movementSpinner = findViewById<Spinner>(R.id.movementSpinner)
        val sleepSpinner = findViewById<Spinner>(R.id.sleepSpinner)
        val brainFogSpinner = findViewById<Spinner>(R.id.brainFogSpinner)
        val morningCheckBox = findViewById<CheckBox>(R.id.morningCheckBox)
        val eveningCheckBox = findViewById<CheckBox>(R.id.eveningCheckBox)
        val nightCheckBox = findViewById<CheckBox>(R.id.nightCheckBox)
        val allDayCheckBox = findViewById<CheckBox>(R.id.allDayCheckBox)
        val nextButton = findViewById<Button>(R.id.nextButton)
        val returnToTopButton = findViewById<Button>(R.id.returnToTopButton)
        var selectedPainLevel = ""
        var selectedMovement = ""
        var selectedSleep = ""
        var selectedBrainFog = ""

        dateTimeText.text = dateFormat.format(Date())
        loadRecentSteps()
        SpinnerHelper.setupSpinner(this, painLevelSpinner, R.array.pain_level_options) {
            selectedPainLevel = it
        }
        SpinnerHelper.setupSpinner(this, movementSpinner, R.array.movement_options) {
            selectedMovement = it
        }
        SpinnerHelper.setupSpinner(this, sleepSpinner, R.array.sleep_options) {
            selectedSleep = it
        }
        SpinnerHelper.setupSpinner(this, brainFogSpinner, R.array.brain_fog_options) {
            selectedBrainFog = it
        }

        nextButton.setOnClickListener {
            val intent = Intent(this, NextActivity::class.java).apply {
                putExtra("date_time", dateTimeText.text.toString())
                putExtra("pain_level", selectedPainLevel)
                putExtra("movement", selectedMovement)
                putExtra("sleep", selectedSleep)
                putExtra("brain_fog", selectedBrainFog)
                putExtra("memo", memoEditText.text.toString())
                putExtra(
                    "pain_time",
                    getSelectedPainTimes(
                        morningCheckBox,
                        eveningCheckBox,
                        nightCheckBox,
                        allDayCheckBox
                    )
                )
            }
            startActivity(intent)
        }

        returnToTopButton.setOnClickListener {
            returnToTop()
        }
    }

    private fun getSelectedPainTimes(vararg checkBoxes: CheckBox): String {
        return checkBoxes
            .filter { it.isChecked }
            .joinToString("、") { it.text.toString() }
    }

    private fun loadRecentSteps() {
        val healthConnectClient = HealthConnectClient.getOrCreate(this)

        lifecycleScope.launch {
            val todayStart = ZonedDateTime.now()
                .toLocalDate()
                .atStartOfDay(ZonedDateTime.now().zone)

            val yesterdayStart = todayStart.minusDays(1)
            val dayBeforeYesterdayStart = todayStart.minusDays(2)

            val yesterdayResponse = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        yesterdayStart.toInstant(),
                        todayStart.toInstant()
                    )
                )
            )

            val dayBeforeYesterdayResponse = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        dayBeforeYesterdayStart.toInstant(),
                        yesterdayStart.toInstant()
                    )
                )
            )

            val yesterdaySteps = yesterdayResponse.records.sumOf { it.count }
            val dayBeforeYesterdaySteps =
                dayBeforeYesterdayResponse.records.sumOf { it.count }

            findViewById<TextView>(R.id.yesterdayStepsText).text =
                "昨日の歩数：${yesterdaySteps} 歩"

            findViewById<TextView>(R.id.dayBeforeYesterdayStepsText).text =
                "一昨日の歩数：${dayBeforeYesterdaySteps} 歩"
        }
    }
}
