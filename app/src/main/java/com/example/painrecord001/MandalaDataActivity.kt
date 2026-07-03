package com.example.painrecord001

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MandalaDataActivity : AppCompatActivity() {
    private val cellSizeDp = 48
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private var selectedPeriodDays = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mandala_data)

        val mandalaPeriodText = findViewById<TextView>(R.id.mandalaPeriodText)
        val mandalaDataText = findViewById<TextView>(R.id.mandalaDataText)
        val mandalaMapView = findViewById<MandalaMapView>(R.id.mandalaMapView)
        val currentPositionItems = findViewById<LinearLayout>(R.id.mandalaCurrentPositionLegendItems)
        val legendItems = findViewById<LinearLayout>(R.id.mandalaColorLegendItems)
        val periodButtons = listOf(
            findViewById<Button>(R.id.periodOneWeekButton) to 7,
            findViewById<Button>(R.id.periodTwoWeeksButton) to 14,
            findViewById<Button>(R.id.periodFourWeeksButton) to 28,
            findViewById<Button>(R.id.periodEightWeeksButton) to 56,
            findViewById<Button>(R.id.periodTwelveWeeksButton) to 84
        )
        val returnToTopButton = findViewById<Button>(R.id.mandalaReturnToTopButton)

        setupCurrentPositionLegend(currentPositionItems)
        setupColorLegend(legendItems)
        setupPeriodButtons(periodButtons, mandalaPeriodText, mandalaDataText, mandalaMapView)
        updateMandalaDisplay(mandalaPeriodText, mandalaDataText, mandalaMapView, selectedPeriodDays)
        returnToTopButton.setOnClickListener {
            returnToTop()
        }
    }

    private fun setupCurrentPositionLegend(currentPositionItems: LinearLayout) {
        currentPositionItems.removeAllViews()
        currentPositionItems.addView(
            createStarLegendRow(getString(R.string.mandala_current_position_latest))
        )
    }

    private fun setupColorLegend(legendItems: LinearLayout) {
        val items = listOf(
            Color.rgb(0, 0, 0) to getString(R.string.mandala_color_legend_one_week),
            Color.rgb(194, 57, 52) to getString(R.string.mandala_color_legend_two_weeks),
            Color.rgb(116, 74, 168) to getString(R.string.mandala_color_legend_four_weeks),
            Color.rgb(16, 142, 113) to getString(R.string.mandala_color_legend_eight_weeks),
            Color.rgb(31, 76, 142) to getString(R.string.mandala_color_legend_twelve_weeks)
        )

        legendItems.removeAllViews()
        for ((color, label) in items) {
            legendItems.addView(createLegendRow(color, label))
        }
    }

    private fun createLegendRow(color: Int, label: String): LinearLayout {
        val density = resources.displayMetrics.density
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (6f * density).toInt()
            }
        }

        val circleSize = (14f * density).toInt()
        val circle = ColorCircleView(this).apply {
            setCircleColor(color)
            layoutParams = LinearLayout.LayoutParams(circleSize, circleSize)
        }
        val text = TextView(this).apply {
            setText(label)
            setTextColor(getColor(R.color.text_primary))
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = (10f * density).toInt()
            }
        }

        row.addView(circle)
        row.addView(text)
        return row
    }

    private fun createStarLegendRow(label: String): LinearLayout {
        val density = resources.displayMetrics.density
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (6f * density).toInt()
            }
        }

        val star = TextView(this).apply {
            text = "★"
            setTextColor(Color.rgb(245, 184, 32))
            textSize = 15f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                (14f * density).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val text = TextView(this).apply {
            setText(label)
            setTextColor(getColor(R.color.text_primary))
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = (10f * density).toInt()
            }
        }

        row.addView(star)
        row.addView(text)
        return row
    }

    private fun setupPeriodButtons(
        periodButtons: List<Pair<Button, Int>>,
        mandalaPeriodText: TextView,
        mandalaDataText: TextView,
        mandalaMapView: MandalaMapView
    ) {
        for ((button, days) in periodButtons) {
            button.setOnClickListener {
                selectedPeriodDays = days
                updatePeriodButtons(periodButtons)
                updateMandalaDisplay(
                    mandalaPeriodText,
                    mandalaDataText,
                    mandalaMapView,
                    selectedPeriodDays
                )
            }
        }
        updatePeriodButtons(periodButtons)
    }

    private fun updatePeriodButtons(periodButtons: List<Pair<Button, Int>>) {
        for ((button, days) in periodButtons) {
            val isSelected = days == selectedPeriodDays
            button.backgroundTintList = ColorStateList.valueOf(
                getColor(if (isSelected) R.color.accent else R.color.clear_button)
            )
            button.setTextColor(getColor(if (isSelected) R.color.white else R.color.text_primary))
        }
    }

    private fun updateMandalaDisplay(
        mandalaPeriodText: TextView,
        mandalaDataText: TextView,
        mandalaMapView: MandalaMapView,
        periodDays: Int
    ) {
        val records = getRecordsInPeriod(periodDays)
        mandalaPeriodText.text = buildPeriodText(records)
        mandalaDataText.text = buildMandalaText(records)
        showMandalaMap(mandalaMapView, records)
    }

    private fun buildPeriodText(records: List<JSONObject>): String {
        val dates = records.mapNotNull { record ->
            parseRecordDate(record)
        }
        if (dates.isEmpty()) {
            return getString(R.string.mandala_period_no_records)
        }

        val oldestDate = dates.minOrNull() ?: return getString(R.string.mandala_period_no_records)
        val newestDate = dates.maxOrNull() ?: return getString(R.string.mandala_period_no_records)
        return getString(
            R.string.mandala_period_format,
            dateOnlyFormat.format(oldestDate),
            dateOnlyFormat.format(newestDate)
        )
    }

    private fun buildMandalaText(records: List<JSONObject>): String {
        if (records.isEmpty()) {
            return getString(R.string.no_saved_records)
        }

        return buildString {
            for ((displayIndex, record) in records.withIndex()) {
                val painNumber = extractLeadingNumber(record.optString("pain_level"))
                val movementNumber = extractLeadingNumber(record.optString("movement"))
                appendLine(
                    getString(
                        R.string.mandala_record_format,
                        displayIndex + 1,
                        painNumber,
                        movementNumber
                    )
                )
            }
        }.trim()
    }

    private fun getRecordsInPeriod(periodDays: Int): List<JSONObject> {
        val records = DiaryRecordStorage.getRecords(this)
        val startDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -(periodDays - 1))
        }.time
        val endDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val selectedRecords = mutableListOf<JSONObject>()

        for (index in records.length() - 1 downTo 0) {
            val record = records.getJSONObject(index)
            val recordDate = parseRecordDate(record) ?: continue

            if (!recordDate.before(startDate) && !recordDate.after(endDate)) {
                selectedRecords.add(record)
            }
        }

        return selectedRecords
    }

    private fun parseRecordDate(record: JSONObject): Date? {
        return runCatching {
            dateFormat.parse(record.optString("date_time"))
        }.getOrNull()
    }

    private fun extractLeadingNumber(value: String): String {
        return Regex("^\\d+").find(value)?.value.orEmpty()
    }

    private fun showMandalaMap(mapView: MandalaMapView, records: List<JSONObject>) {
        val points = records.mapIndexedNotNull { displayIndex, record ->
            val pain = extractLeadingNumber(record.optString("pain_level")).toIntOrNull()
            val movement = extractLeadingNumber(record.optString("movement")).toIntOrNull()
            val recordDate = parseRecordDate(record)
            if (pain != null && movement != null && recordDate != null && pain in 1..6 && movement in 1..6) {
                MandalaMapView.Point(
                    pain = pain,
                    movement = movement,
                    label = if (displayIndex == 0) "★" else (displayIndex + 1).toString(),
                    color = colorForRecordDate(recordDate),
                    isLatest = displayIndex == 0
                )
            } else {
                null
            }
        }
        mapView.setPoints(points)
    }

    private fun colorForRecordDate(recordDate: Date): Int {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val daysAgo = ((todayStart.time - recordDate.time) / MILLIS_PER_DAY).toInt()
        return when {
            daysAgo < 7 -> Color.rgb(0, 0, 0)
            daysAgo < 14 -> Color.rgb(194, 57, 52)
            daysAgo < 28 -> Color.rgb(116, 74, 168)
            daysAgo < 56 -> Color.rgb(16, 142, 113)
            else -> Color.rgb(31, 76, 142)
        }
    }

    companion object {
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}
