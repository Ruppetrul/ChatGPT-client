package com.example.chatgpt

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SplashActivity : AppCompatActivity() {

    var button: Button? = null
    var preloader: ImageView? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем LinearLayout, который будет содержать TextView и кнопку
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.gravity = Gravity.CENTER
        linearLayout.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // Создаем TextView с надписью "Chat GPT"
        val textView = TextView(this)
        textView.text = "Chat GPT"
        textView.textSize = 24f
        textView.gravity = Gravity.CENTER
        val textLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textLayoutParams.gravity = Gravity.CENTER
        textView.layoutParams = textLayoutParams
        linearLayout.addView(textView)

        // Создаем кнопку "Try again"
        button = Button(this)
        button!!.visibility = View.INVISIBLE
        button!!.text = "Try again"

        // Создаем TextView с надписью "Chat GPT"
        preloader = ImageView(this)
        preloader!!.setImageResource(R.drawable.ic_loading)

        button!!.setOnClickListener {
            button!!.visibility = View.INVISIBLE
            preloader!!.visibility = View.VISIBLE
            // обработка нажатия кнопки
            val apiKey = getTokenFromSharedPreferences(this@SplashActivity)
            if (apiKey != null) {
                process(apiKey)
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Устанавливаем LayoutParams для кнопки, чтобы она была расположена по центру экрана
        val buttonLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonLayoutParams.gravity = Gravity.CENTER
        button!!.layoutParams = buttonLayoutParams
        linearLayout.addView(button)

        //preloader
        linearLayout.addView(preloader)

        setContentView(linearLayout)

        supportActionBar?.hide()

        val apiKey = getTokenFromSharedPreferences(this@SplashActivity)
        if (apiKey != null) {
            process(apiKey)
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

    private fun process(apiKey : String) {
        getAvailableModels(apiKey,
            onSuccess = { modelNames ->
                preloader!!.visibility = View.INVISIBLE
                val intent = Intent(this, Chat::class.java)

                intent.putExtra("models", modelNames)

                startActivity(intent)
                finish()
            },
            onFailure = { exception ->
                runOnUiThread {
                    preloader!!.visibility = View.INVISIBLE
                    button!!.visibility = View.VISIBLE
                    Toast.makeText(this@SplashActivity, "Internet problems or Open AI is not available. :(", Toast.LENGTH_SHORT).show()
                    println("Error: ${exception.message}")
                }
            }
        )
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
