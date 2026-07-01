package com.example.painrecord001

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class SavedRecordsActivity : AppCompatActivity() {
    private lateinit var recordsContainer: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_records)

        recordsContainer = findViewById(R.id.recordsContainer)
        emptyText = findViewById(R.id.emptyRecordsText)
        findViewById<Button>(R.id.savedRecordsReturnToTopButton).setOnClickListener {
            returnToTop()
        }

        showRecords()
    }

    private fun showRecords() {
        recordsContainer.removeAllViews()
        val records = DiaryRecordStorage.getRecords(this)
        emptyText.visibility = if (records.length() == 0) View.VISIBLE else View.GONE

        for (index in records.length() - 1 downTo 0) {
            val record = records.getJSONObject(index)
            recordsContainer.addView(createRecordView(record, index))
        }
    }

    private fun createRecordView(record: JSONObject, index: Int): View {
        val itemView = layoutInflater.inflate(R.layout.item_saved_record, recordsContainer, false)
        val recordText = itemView.findViewById<TextView>(R.id.recordText)
        val deleteButton = itemView.findViewById<Button>(R.id.deleteRecordButton)

        recordText.text = buildRecordSummary(record)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    DiaryRecordStorage.deleteRecord(this, index)
                    showRecords()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        return itemView
    }

    private fun buildRecordSummary(record: JSONObject): String {
        return buildString {
            appendLine("${getString(R.string.date_time_label)}: ${record.optString("date_time", getString(R.string.unknown_date_time))}")
            appendLine("${getString(R.string.pain_level_label)}: ${record.optString("pain_level")}")
            appendLine("${getString(R.string.movement_label)}: ${record.optString("movement")}")
            appendLine("${getString(R.string.sleep_label)}: ${record.optString("sleep")}")
            appendLine("${getString(R.string.brain_fog_label)}: ${record.optString("brain_fog")}")
            appendLine("${getString(R.string.pain_time_label)}: ${record.optString("pain_time")}")
            appendLine("${getString(R.string.weather_label)}: ${record.optString("weather")}")
            appendLine("${getString(R.string.temperature_label)}: ${record.optString("temperature")}")
            appendLine("${getString(R.string.pressure_label)}: ${record.optString("pressure")}")
            appendLine("${getString(R.string.daily_activity_label)}: ${record.optString("daily_activity")}")
            appendLine("${getString(R.string.memo_label)}: ${record.optString("memo")}")
        }.trim()
    }
}
