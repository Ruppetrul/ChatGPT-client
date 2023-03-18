package com.example.chatgpt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val api_key = getTokenFromSharedPreferences(this@MainActivity);

        val isNullOrEmpty = api_key.isNullOrEmpty()
        if (!isNullOrEmpty) {
            goToChatActivity(this@MainActivity)
        }

        editText = findViewById(R.id.api_key_edit_text)
        button = findViewById(R.id.save_button)

        button.setOnClickListener {
            val key = editText.text.toString()

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("https://api.openai.com/v1/engines/text-davinci-003/completions")
                        .header("Authorization", "Bearer $key")
                        .post(
                            RequestBody.create(
                                "application/json".toMediaTypeOrNull(),
                                """
                        {
                            "prompt": "Say it test!",
                            "temperature": 0.5,
                            "max_tokens": 1,
                            "top_p": 1,
                            "frequency_penalty": 0,
                            "presence_penalty": 0
                        }
                    """.trimIndent()
                            )
                        )
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            // Обработка ошибки
                            Log.e("Error", "Ошибка запроса", e)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseData = response.body?.string()
                            if (responseData.isNullOrEmpty()) {
                                // Обработка ошибки - ответ пустой
                                Log.e("Error", "Ошибка запроса: ответ пустой")
                            } else {
                                try {
                                    val jsonObject = JSONObject(responseData)
                                    if (jsonObject.has("error")) {
                                        // Обработка наличия ключа "error"
                                        println("В ответе есть ключ error")
                                        Log.d("ERROR", jsonObject.getString("error"))
                                        showToast("Ошибка: ключ недействителен")
                                    } else {
                                        // Обработка отсутствия ключа "error"
                                        println("В ответе нет ключа error")

                                        saveTokenToSharedPreferences(this@MainActivity, key);
                                        goToChatActivity(this@MainActivity)
                                        // Обработка успешного ответа
                                        Log.d("Error", "Ответ: $responseData")
                                    }
                                } catch (e: JSONException) {
                                    // Обработка ошибки
                                    println("Ошибка парсинга JSON: ${e.message}")
                                }
                                val error = responseData


                            }
                        }
                    })
                } catch (e: IOException) {
                    showToast("Ошибка: ${e.message}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun saveTokenToSharedPreferences(context: Context, token: String) {
        val prefs = context.getSharedPreferences(Companion.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(Companion.TOKEN_KEY, token).apply()
    }

    fun goToChatActivity(context: Context) {
        val intent = Intent(context, Chat::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
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
