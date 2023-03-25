package com.example.chatgpt

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем TextView с надписью "Chat GPT"
        val textView = TextView(this)
        textView.text = "Chat GPT"
        textView.textSize = 24f
        textView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        textView.gravity = Gravity.CENTER

        setContentView(textView)

        supportActionBar?.hide()

        // Создаем таймер на 2 секунды
        Handler().postDelayed({
            // Запускаем MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}
