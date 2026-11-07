
package com.tetsushozawa.paincompass

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class PressureRecordingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var repository: PressureRepository

    private val handler = Handler(Looper.getMainLooper())
    private var completed = false

    private val timeoutTask = Runnable {
        finishMeasurement()
    }

    override fun onCreate() {
        super.onCreate()

        repository = PressureRepository(this)

        sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val pressureSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        if (pressureSensor == null) {
            finishMeasurement()
            return
        }

        handler.postDelayed(timeoutTask, 10000L)

        val registered = sensorManager.registerListener(
            this,
            pressureSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        if (!registered) {
            finishMeasurement()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (completed || event.sensor.type != Sensor.TYPE_PRESSURE) {
            return
        }

        val pressure = event.values.firstOrNull() ?: return

        if (!pressure.isFinite() || pressure <= 0f) {
            return
        }

        completed = true

        repository.insert(
            PressureReading(
                timestamp = System.currentTimeMillis(),
                pressureHpa = pressure
            )
        )

        finishMeasurement()
    }

    private fun finishMeasurement() {
        if (completed) {
            // 保存済みの場合も停止処理を行う
        } else {
            completed = true
        }

        handler.removeCallbacks(timeoutTask)
        sensorManager.unregisterListener(this)
        stopSelf()
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) = Unit

    override fun onDestroy() {
        handler.removeCallbacks(timeoutTask)
        sensorManager.unregisterListener(this)
        repository.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
