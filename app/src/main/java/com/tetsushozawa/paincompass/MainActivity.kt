package com.tetsushozawa.paincompass

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainScrollView = findViewById<NestedScrollView>(R.id.mainScrollView)
        ViewCompat.setOnApplyWindowInsetsListener(mainScrollView) { view, windowInsets ->
            val navigationBarInsets =
                windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navigationBarInsets.bottom)
            windowInsets
        }
        ViewCompat.requestApplyInsets(mainScrollView)

        findViewById<Button>(R.id.openRecordInputButton).setOnClickListener {
            startActivity(Intent(this, RecordInputActivity::class.java))
        }

        findViewById<Button>(R.id.openSavedRecordsButton).setOnClickListener {
            startActivity(Intent(this, SavedRecordsActivity::class.java))
        }

        findViewById<Button>(R.id.openMandalaDataButton).setOnClickListener {
            startActivity(Intent(this, MandalaDataActivity::class.java))
        }
    }
}
