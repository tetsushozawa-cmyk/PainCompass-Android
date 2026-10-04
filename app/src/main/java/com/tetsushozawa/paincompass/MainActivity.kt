package com.tetsushozawa.paincompass

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

import androidx.core.widget.NestedScrollView
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import java.time.ZonedDateTime
import androidx.lifecycle.lifecycleScope
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.launch
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.PermissionController

class MainActivity : AppCompatActivity() {
    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )
    private val requestPermission =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted ->
            if (granted.containsAll(permissions)) {
                loadYesterdaySteps()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val yesterdayStepsText = findViewById<TextView>(R.id.yesterdayStepsText)
        val healthConnectClient = HealthConnectClient.getOrCreate(this)
        lifecycleScope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (!granted.containsAll(permissions)) {
                requestPermission.launch(permissions)
            } else {
                loadYesterdaySteps()
            }
        }


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
        private fun loadYesterdaySteps() {


            val healthConnectClient = HealthConnectClient.getOrCreate(this)
            lifecycleScope.launch {
                val endTime = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone)
                val startTime = endTime.minusDays(1)
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            startTime.toInstant(),
                            endTime.toInstant()
                        )
                    )
                )
                val totalSteps = response.records.sumOf { it.count }
                val yesterdayStepsText = findViewById<TextView>(R.id.yesterdayStepsText)
                yesterdayStepsText.text = "昨日の歩数: ${totalSteps} 歩"
            }
    }
}
