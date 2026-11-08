
package com.tetsushozawa.paincompass

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class PressureMandalaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pressure_mandala)

        val mapView = findViewById<MandalaMapView>(
            R.id.pressureMandalaMapView
        )
        val recordsText = findViewById<TextView>(
            R.id.pressureMandalaRecordsText
        )

        mapView.setPressureAxis(true)

        findViewById<Button>(
            R.id.pressureMandalaBackButton
        ).setOnClickListener {
            finish()
        }

        val savedRecords = DiaryRecordStorage.getRecords(this)
        val validRecords = mutableListOf<JSONObject>()

        for (i in 0 until savedRecords.length()) {
            val record = savedRecords.optJSONObject(i) ?: continue

            if (!record.has("pressure_change_level") ||
                record.isNull("pressure_change_level")) continue

            val level = record.optInt("pressure_change_level", 99)
            if (level !in -2..2) continue

            val pain = Regex("^\\d+")
                .find(record.optString("pain_level"))
                ?.value?.toIntOrNull()

            if (pain !in 1..6) continue

            validRecords.add(record)
        }

        val dateFormat = SimpleDateFormat(
            "yyyy/MM/dd HH:mm",
            Locale.getDefault()
        )

        val sortedRecords = validRecords.sortedByDescending {
            runCatching {
                dateFormat.parse(
                    it.optString("date_time")
                )?.time ?: 0L
            }.getOrDefault(0L)
        }

        val points = sortedRecords.mapIndexed { index, record ->
            val pain = Regex("^\\d+")
                .find(record.optString("pain_level"))!!
                .value.toInt()

            val level = record.optInt("pressure_change_level")

            MandalaMapView.Point(
                pain = pain,
                movement = level,
                label = "${index + 1}",
                color = Color.rgb(31, 76, 142),
                isLatest = index == 0
            )
        }

        mapView.setPoints(points)

        recordsText.text = if (points.isEmpty()) {
            "気圧差を記録したデータはまだありません"
        } else {
            "気圧変化と疼痛の記録：${points.size}件"
        }
    }
}
