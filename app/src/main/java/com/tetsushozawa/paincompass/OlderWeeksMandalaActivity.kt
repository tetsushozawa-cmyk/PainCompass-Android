
package com.tetsushozawa.paincompass

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OlderWeeksMandalaActivity : AppCompatActivity() {

    private val dateFormat =
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_older_weeks_mandala)

        val periodText =
            findViewById<TextView>(R.id.olderWeeksPeriodText)
        val recordsText =
            findViewById<TextView>(R.id.olderWeeksRecordsText)
        val mapView =
            findViewById<MandalaMapView>(R.id.olderWeeksMapView)
        val backButton =
            findViewById<Button>(R.id.olderWeeksBackButton)

        val records = getOlderRecords()

        periodText.text = "13〜26週の記録：${records.size}件"

        if (records.isEmpty()) {
            recordsText.text = "この期間の記録はありません"
        } else {
            recordsText.text = records.mapIndexed { index, record ->
                val pain = leadingNumber(record.optString("pain_level"))
                val movement = leadingNumber(record.optString("movement"))
                "${index + 1}. 痛み$pain・活動$movement"
            }.joinToString("\n")
        }

        val points = records.mapIndexedNotNull { index, record ->
            val pain = leadingNumber(
                record.optString("pain_level")
            ).toIntOrNull()

            val movement = leadingNumber(
                record.optString("movement")
            ).toIntOrNull()

            if (pain in 1..6 && movement in 1..6) {
                MandalaMapView.Point(
                    pain = pain!!,
                    movement = movement!!,
                    label = if (index == 0) "★" else "${index + 1}",
                    color = android.graphics.Color.rgb(31, 76, 142),
                    isLatest = index == 0
                )
            } else {
                null
            }
        }

        mapView.setPoints(points)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun getOlderRecords(): List<JSONObject> {
        val records = DiaryRecordStorage.getRecords(this)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = (today.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -181)
        }.time

        val end = (today.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -84)
        }.time

        val result = mutableListOf<JSONObject>()

        for (i in records.length() - 1 downTo 0) {
            val record = records.getJSONObject(i)
            val date = runCatching {
                dateFormat.parse(record.optString("date_time"))
            }.getOrNull() ?: continue

            if (!date.before(start) && date.before(end)) {
                result.add(record)
            }
        }

        return result
    }

    private fun leadingNumber(value: String): String {
        return Regex("^\\d+").find(value)?.value.orEmpty()
    }
}
