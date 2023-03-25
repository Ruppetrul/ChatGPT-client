package com.example.chatgpt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

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

        val apiKey = getTokenFromSharedPreferences(this@SplashActivity)
        if (apiKey != null) {
            getAvailableModels(apiKey,
                onSuccess = { modelNames ->
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onFailure = { exception ->
                    runOnUiThread {
                        Toast.makeText(this@SplashActivity, "Internet problems or Open AI is not available. :(", Toast.LENGTH_SHORT).show()
                        println("Error: ${exception.message}")
                    }
                }
            )
        } else {
            // Создаем таймер на 2 секунды
            Handler().postDelayed({
                // Запускаем MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000)
        }
    }


    fun getAvailableModels(apiKey: String, onSuccess: (Array<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.openai.com/v1/models")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val modelsArray = jsonObject.getJSONArray("data")
                    val modelNames = Array(modelsArray.length()) { i -> modelsArray.getJSONObject(i).getString("id") }
                    onSuccess(modelNames)
                } else {
                    onFailure(IOException("Unexpected response code ${response.code}"))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onFailure(e)
            }
        })
    }


    fun getTokenFromSharedPreferences(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(TOKEN_KEY, null)
    }

    companion object {
        private const val PREFS_NAME = "myPrefs"
        private const val TOKEN_KEY = "token"
    }
}
