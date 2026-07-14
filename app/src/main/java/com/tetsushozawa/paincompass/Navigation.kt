package com.tetsushozawa.paincompass

import android.app.Activity
import android.content.Intent

fun Activity.returnToTop() {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    startActivity(intent)
    finish()
}
