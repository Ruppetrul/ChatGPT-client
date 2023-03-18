package com.example.chatgpt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class Chat : AppCompatActivity() {

    private val TAG = "Chat"
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button

    private lateinit var messageList: MutableList<Message>
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        getAvailableModels()

        // Инициализируем ViewModel
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        // Получаем список сообщений из ViewModel
        messageList = chatViewModel.messageList

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        messageRecyclerView = findViewById(R.id.recycler_view_messages)
        messageEditText = findViewById(R.id.edit_text_chat_input)
        sendButton = findViewById(R.id.button_chat_send)

        messageAdapter = MessageAdapter(this@Chat, messageList)
        messageRecyclerView.adapter = messageAdapter
        messageRecyclerView.layoutManager = LinearLayoutManager(this)

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                chatViewModel!!.messageList.add(Message(message, "You",true, 0))
                messageAdapter.notifyDataSetChanged()
                messageEditText.setText("")
                sendMessage(message)
            }
        }

        //getModelsFromOpenAiApi()
    }

    private fun sendMessage(message: String) {
        val client = OkHttpClient()

        val key = getTokenFromSharedPreferences(this@Chat)
        val request = Request.Builder()
            .url("https://api.openai.com/v1/engines/text-davinci-003/completions")
            .addHeader("Content-Type", "application/json")

            .addHeader("Authorization", "Bearer $key")
            .post(
                RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    """
                        {
                            "prompt": "$message",
                            "temperature": 0.5,
                            "max_tokens": 150,
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
                runOnUiThread {
                    showToast("Failed to send message")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()
                val jsonObject = JSONObject(responseText)
                if (jsonObject.has("error")) {
                    val errorObject = jsonObject.getJSONObject("error")
                    val errorMessage = errorObject.getString("message")
                    val errorType = errorObject.getString("type")

                    // handle insufficient quota error
                    runOnUiThread {
                        Toast.makeText(this@Chat, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    return

                }
                val choices = jsonObject.getJSONArray("choices")

                val text = choices.getJSONObject(0).getString("text")

                var totalTokens = 0
                if (jsonObject.has("usage")) {
                    val usage = jsonObject.getJSONObject("usage")
                    totalTokens = usage.getInt("total_tokens")
                }

                runOnUiThread {
//                    chatViewModel.messageList.add(Message(message, "You",true, 0))

                    chatViewModel.messageList.add(Message(text, "Gpt", false, totalTokens))
                    messageAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    fun getTokenFromSharedPreferences(context: Context): String? {
        val prefs = context.getSharedPreferences(Companion.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(Companion.TOKEN_KEY, null)
    }

    fun removeTokenFromSharedPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(TOKEN_KEY)
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "myPrefs"
        private const val TOKEN_KEY = "token"
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Обрабатываем нажатие на кнопку "Назад" в статус-баре
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                removeTokenFromSharedPreferences(this@Chat)
                // Здесь происходит переход на другую активность при нажатии на кнопку
                val intent = Intent(this@Chat, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getAvailableModels() {
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            val key = getTokenFromSharedPreferences(this@Chat)

            // Формируем запрос к API OpenAI
            val request = Request.Builder()
                .url("https://api.openai.com/v1/models")
                .addHeader("Content-Type", "application/json")

                .addHeader("Authorization", "Bearer $key")
                .build()

            // Отправляем запрос и получаем ответ
            val response = client.newCall(request).execute()
            val responseString = response.body?.string()

            // Обрабатываем ответ
            if (response.isSuccessful && !responseString.isNullOrEmpty()) {
                val jsonObject = JSONObject(responseString)
                val modelsArray = jsonObject.getJSONArray("data")
                val modelsList = mutableListOf<String>()
                for (i in 0 until modelsArray.length()) {
                    modelsList.add(modelsArray.getJSONObject(i).getString("id"))
                }

                val list = modelsList.toTypedArray()
                for (s in list) {
                    Log.d("TAG", s)
                }
            }
        }
    }

}

data class Model(
    val name: String,
    val description: String
)



