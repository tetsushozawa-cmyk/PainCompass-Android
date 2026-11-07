package com.tetsushozawa.paincompass

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PressureHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = PressureRepository(this)
        val readings = repository.readingsForToday()

        val scrollView = ScrollView(this)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "今日の気圧記録"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        container.addView(title)

        val header = TextView(this).apply {
            text = "時刻　　　　気圧"
            textSize = 18f
            setPadding(0, 8, 0, 8)
        }
        container.addView(header)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (readings.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "まだ今日の気圧記録はありません"
                textSize = 18f
                setPadding(0, 24, 0, 0)
            }
            container.addView(emptyText)
        } else {
            readings.forEach { reading ->

                val row = TextView(this).apply {
                    val time = timeFormat.format(Date(reading.timestamp))
                    text = String.format(
                        Locale.getDefault(),
                        "%s　　　　%.1f hPa",
                        time,
                        reading.pressureHpa
                    )
                    textSize = 18f
                    setPadding(0, 12, 0, 12)
                }

                container.addView(row)
            }
        }

        scrollView.addView(container)
        setContentView(scrollView)
    }
}