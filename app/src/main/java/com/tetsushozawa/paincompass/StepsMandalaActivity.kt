
package com.tetsushozawa.paincompass

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import org.json.JSONObject
import java.text.SimpleDateFormat
class StepsMandalaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps_mandala)

        val yesterdayButton =
            findViewById<Button>(R.id.yesterdayStepsButton)

        val beforeYesterdayButton =
            findViewById<Button>(R.id.beforeYesterdayStepsButton)

        val periodText =
            findViewById<TextView>(R.id.stepsMandalaPeriodText)


        val mapView =
            findViewById<MandalaMapView>(R.id.stepsMandalaMapView)

        mapView.setStepsAxis(true)

        val backButton =
            findViewById<Button>(R.id.stepsMandalaBackButton)

        yesterdayButton.setOnClickListener {
            periodText.text = "昨日の歩数と今日の疼痛"
        }

        beforeYesterdayButton.setOnClickListener {
            periodText.text = "一昨日の歩数と今日の疼痛"
        }

        backButton.setOnClickListener {
            finish()
        }

        val savedRecords = DiaryRecordStorage.getRecords(this)

        fun showStepsMandala(useYesterday: Boolean) {
            val stepKey = if (useYesterday) {
                "yesterday_steps"
            } else {
                "day_before_yesterday_steps"
            }

            periodText.text = if (useYesterday) {
                "昨日の歩数と疼痛"
            } else {
                "一昨日の歩数と疼痛"
            }

            val validRecords = mutableListOf<JSONObject>()

            for (i in 0 until savedRecords.length()) {
                val record = savedRecords.optJSONObject(i) ?: continue

                if (!record.has(stepKey) || record.isNull(stepKey)) {
                    continue
                }

                val pain = Regex("^\\d+")
                    .find(record.optString("pain_level"))
                    ?.value?.toIntOrNull()

                if (pain !in 1..6) continue

                validRecords.add(record)
            }

            val sortedRecords = validRecords.sortedByDescending {
                runCatching {
                    SimpleDateFormat(
                        "yyyy/MM/dd HH:mm",
                        java.util.Locale.getDefault()
                    ).parse(it.optString("date_time"))?.time ?: 0L
                }.getOrDefault(0L)
            }

            val points = sortedRecords.mapIndexed { index, record ->
                val pain = Regex("^\\d+")
                    .find(record.optString("pain_level"))!!
                    .value.toInt()

                val steps = record.optLong(stepKey)


                android.util.Log.d("StepsMandala", "記録${index + 1}：${record.optString("date_time")}・${steps}歩・痛み$pain")





                val level = when {
                    steps <= 500L -> 1
                    steps <= 1000L -> 2
                    steps <= 2500L -> 3
                    steps <= 5000L -> 4
                    steps <= 8000L -> 5
                    else -> 6
                }

                MandalaMapView.Point(
                    pain = pain,
                    movement = level,
                    label = "${index + 1}",
                    color = Color.rgb(31, 76, 142),
                    isLatest = index == 0
                )
            }

            mapView.setPoints(points)

            periodText.text = "${periodText.text}：${points.size}件"

            findViewById<TextView>(
                R.id.stepsMandalaRecordsText
            ).text = if (points.isEmpty()) {
                "この条件の記録はありません"
            } else {
                "記録を${points.size}件表示しています"
            }
        }

        yesterdayButton.setOnClickListener {
            showStepsMandala(true)
        }

        beforeYesterdayButton.setOnClickListener {
            showStepsMandala(false)
        }

        showStepsMandala(true)

    }
}
