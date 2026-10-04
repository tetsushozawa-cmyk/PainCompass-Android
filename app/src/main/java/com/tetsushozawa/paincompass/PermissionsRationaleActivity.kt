package com.tetsushozawa.paincompass

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PermissionsRationaleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = """
                Health Connect の利用について

                昨日の歩数を表示するために、歩数データを読み取ります。
            """.trimIndent()

            textSize = 18f
            setPadding(48, 48, 48, 48)
        }

        setContentView(textView)
    }
}