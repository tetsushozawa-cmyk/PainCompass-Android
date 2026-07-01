package com.example.painrecord001

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
