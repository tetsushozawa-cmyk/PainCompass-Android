package com.example.painrecord001

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
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
        val mandalaMapTable = findViewById<TableLayout>(R.id.mandalaMapTable)
        val periodButtons = listOf(
            findViewById<Button>(R.id.periodOneWeekButton) to 7,
            findViewById<Button>(R.id.periodTwoWeeksButton) to 14,
            findViewById<Button>(R.id.periodFourWeeksButton) to 28,
            findViewById<Button>(R.id.periodEightWeeksButton) to 56,
            findViewById<Button>(R.id.periodTwelveWeeksButton) to 84
        )
        val returnToTopButton = findViewById<Button>(R.id.mandalaReturnToTopButton)

        setupPeriodButtons(periodButtons, mandalaPeriodText, mandalaDataText, mandalaMapTable)
        updateMandalaDisplay(mandalaPeriodText, mandalaDataText, mandalaMapTable, selectedPeriodDays)
        returnToTopButton.setOnClickListener {
            returnToTop()
        }
    }

    private fun setupPeriodButtons(
        periodButtons: List<Pair<Button, Int>>,
        mandalaPeriodText: TextView,
        mandalaDataText: TextView,
        mandalaMapTable: TableLayout
    ) {
        for ((button, days) in periodButtons) {
            button.setOnClickListener {
                selectedPeriodDays = days
                updatePeriodButtons(periodButtons)
                updateMandalaDisplay(
                    mandalaPeriodText,
                    mandalaDataText,
                    mandalaMapTable,
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
        mandalaMapTable: TableLayout,
        periodDays: Int
    ) {
        val records = getRecordsInPeriod(periodDays)
        mandalaPeriodText.text = buildPeriodText(records)
        mandalaDataText.text = buildMandalaText(records)
        showMandalaMap(mandalaMapTable, records)
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

    private fun showMandalaMap(table: TableLayout, records: List<JSONObject>) {
        table.removeAllViews()
        val cells = buildMandalaCells(records)

        table.addView(createHeaderRow())
        for (movement in 1..6) {
            val row = TableRow(this)
            row.addView(createCell("動$movement", isHeader = true))
            for (pain in 1..6) {
                row.addView(createCell(cells[movement - 1][pain - 1].joinToString(",")))
            }
            table.addView(row)
        }
    }

    private fun buildMandalaCells(records: List<JSONObject>): Array<Array<MutableList<String>>> {
        val cells = Array(6) { Array(6) { mutableListOf<String>() } }

        for ((displayIndex, record) in records.withIndex()) {
            val pain = extractLeadingNumber(record.optString("pain_level")).toIntOrNull()
            val movement = extractLeadingNumber(record.optString("movement")).toIntOrNull()
            if (pain != null && movement != null && pain in 1..6 && movement in 1..6) {
                cells[movement - 1][pain - 1].add((displayIndex + 1).toString())
            }
        }

        return cells
    }

    private fun createHeaderRow(): TableRow {
        val row = TableRow(this)
        row.addView(createCell("", isHeader = true))
        for (pain in 1..6) {
            row.addView(createCell("痛$pain", isHeader = true))
        }
        return row
    }

    private fun createCell(text: String, isHeader: Boolean = false): TextView {
        val cellSize = (cellSizeDp * resources.displayMetrics.density).toInt()
        return TextView(this).apply {
            layoutParams = TableRow.LayoutParams(cellSize, cellSize)
            background = getDrawable(R.drawable.mandala_cell_background)
            gravity = Gravity.CENTER
            setText(text)
            textSize = if (isHeader) 13f else 16f
            setTextColor(getColor(R.color.text_primary))
            if (isHeader) {
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
        }
    }
}
