package com.tetsushozawa.paincompass

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object DiaryRecordStorage {
    private const val PREFS_NAME = "diary_records"
    private const val RECORDS_KEY = "records_json"

    fun addRecord(context: Context, record: JSONObject) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val records = getRecords(context)
        records.put(record)
        preferences.edit()
            .putString(RECORDS_KEY, records.toString())
            .apply()
    }

    fun getRecords(context: Context): JSONArray {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val recordsText = preferences.getString(RECORDS_KEY, "[]").orEmpty()
        return runCatching { JSONArray(recordsText) }.getOrDefault(JSONArray())
    }

    fun deleteRecord(context: Context, index: Int) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val records = getRecords(context)
        val updatedRecords = JSONArray()

        for (i in 0 until records.length()) {
            if (i != index) {
                updatedRecords.put(records.getJSONObject(i))
            }
        }

        preferences.edit()
            .putString(RECORDS_KEY, updatedRecords.toString())
            .apply()
    }
}
