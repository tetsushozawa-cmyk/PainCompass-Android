package com.example.painrecord001

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordInputActivity : AppCompatActivity() {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_input)

        val dateTimeText = findViewById<TextView>(R.id.dateTimeText)
        val painLevelSpinner = findViewById<Spinner>(R.id.painLevelSpinner)
        val movementSpinner = findViewById<Spinner>(R.id.movementSpinner)
        val sleepSpinner = findViewById<Spinner>(R.id.sleepSpinner)
        val brainFogSpinner = findViewById<Spinner>(R.id.brainFogSpinner)
        val morningCheckBox = findViewById<CheckBox>(R.id.morningCheckBox)
        val eveningCheckBox = findViewById<CheckBox>(R.id.eveningCheckBox)
        val nightCheckBox = findViewById<CheckBox>(R.id.nightCheckBox)
        val allDayCheckBox = findViewById<CheckBox>(R.id.allDayCheckBox)
        val memoEditText = findViewById<EditText>(R.id.memoEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val nextButton = findViewById<Button>(R.id.nextButton)
        val returnToTopButton = findViewById<Button>(R.id.returnToTopButton)

        dateTimeText.text = dateFormat.format(Date())
        setupSpinner(painLevelSpinner, R.array.pain_level_options)
        setupSpinner(movementSpinner, R.array.movement_options)
        setupSpinner(sleepSpinner, R.array.sleep_options)
        setupSpinner(brainFogSpinner, R.array.brain_fog_options)

        saveButton.setOnClickListener {
            val dateTime = dateTimeText.text.toString()
            val painLevel = painLevelSpinner.selectedItem.toString()
            val movement = movementSpinner.selectedItem.toString()
            val sleep = sleepSpinner.selectedItem.toString()
            val brainFog = brainFogSpinner.selectedItem.toString()
            val painTime = getSelectedPainTimes(
                morningCheckBox,
                eveningCheckBox,
                nightCheckBox,
                allDayCheckBox
            )
            val memo = memoEditText.text.toString()

            getSharedPreferences("diary_records", MODE_PRIVATE)
                .edit()
                .putString("latest_date_time", dateTime)
                .putString("latest_pain_level", painLevel)
                .putString("latest_movement", movement)
                .putString("latest_sleep", sleep)
                .putString("latest_brain_fog", brainFog)
                .putString("latest_pain_time", painTime)
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
                    .put("weather", "")
                    .put("temperature", "")
                    .put("pressure", "")
                    .put("daily_activity", "")
                    .put("memo", memo)
            )

            Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show()
        }

        nextButton.setOnClickListener {
            val intent = Intent(this, NextActivity::class.java).apply {
                putExtra("date_time", dateTimeText.text.toString())
                putExtra("pain_level", painLevelSpinner.selectedItem.toString())
                putExtra("movement", movementSpinner.selectedItem.toString())
                putExtra("sleep", sleepSpinner.selectedItem.toString())
                putExtra("brain_fog", brainFogSpinner.selectedItem.toString())
                putExtra(
                    "pain_time",
                    getSelectedPainTimes(
                        morningCheckBox,
                        eveningCheckBox,
                        nightCheckBox,
                        allDayCheckBox
                    )
                )
                putExtra("memo", memoEditText.text.toString())
            }
            startActivity(intent)
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

    private fun getSelectedPainTimes(vararg checkBoxes: CheckBox): String {
        return checkBoxes
            .filter { it.isChecked }
            .joinToString("、") { it.text.toString() }
    }

}
