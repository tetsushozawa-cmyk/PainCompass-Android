package com.tetsushozawa.paincompass

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PressureRepository(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "pressure.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE pressure_readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                recorded_at INTEGER NOT NULL,
                pressure_hpa REAL NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE INDEX index_pressure_recorded_at " +
                    "ON pressure_readings(recorded_at)"
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) = Unit

    fun insert(reading: PressureReading) {
        writableDatabase.insert(
            "pressure_readings",
            null,
            ContentValues().apply {
                put("recorded_at", reading.timestamp)
                put("pressure_hpa", reading.pressureHpa)
            }
        )
    }

    fun latest(limit: Int = 2): List<PressureReading> =
        readableDatabase.query(
            "pressure_readings",
            arrayOf("recorded_at", "pressure_hpa"),
            null,
            null,
            null,
            null,
            "recorded_at DESC",
            limit.toString()
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        PressureReading(
                            timestamp = cursor.getLong(0),
                            pressureHpa = cursor.getFloat(1)
                        )
                    )
                }
            }
        }
    fun readingsForToday(): List<PressureReading> {
        val calendar = java.util.Calendar.getInstance()

        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        val start = calendar.timeInMillis

        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        val end = calendar.timeInMillis

        return readingsSince(start, end)
    }
    fun readingsSince(
        since: Long,
        until: Long = System.currentTimeMillis()
    ): List<PressureReading> =
        readableDatabase.query(
            "pressure_readings",
            arrayOf("recorded_at", "pressure_hpa"),
            "recorded_at >= ? AND recorded_at <= ?",
            arrayOf(since.toString(), until.toString()),
            null,
            null,
            "recorded_at ASC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        PressureReading(
                            timestamp = cursor.getLong(0),
                            pressureHpa = cursor.getFloat(1)
                        )
                    )
                }
            }
        }
}