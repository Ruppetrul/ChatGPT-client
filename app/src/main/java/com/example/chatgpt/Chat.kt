package com.example.chatgpt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException


class Chat : AppCompatActivity() {

    private val TAG = "Chat"
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button

    private lateinit var messageList: MutableList<Message>
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatViewModel: ChatViewModel

    private lateinit var models : Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Извлекаем массив из Intent
        models = intent.extras?.getStringArray("models") as Array<String>

        val current_model = getModelFromSharedPreferences(this)

        if (current_model == null) {
            val intent = Intent(this@Chat, SelectModel::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

            intent.putExtra("models", models)

            startActivity(intent)
            finish()
        } else {
            title = "Chat GPT ($current_model)"
        }

// Создаем адаптер для списка
        if (models != null) {
            if (models.isNotEmpty()) {
                Log.d("MODELS", "models")

            }
        }

        // Инициализируем ViewModel
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        // Получаем список сообщений из ViewModel
        messageList = chatViewModel.messageList

        messageRecyclerView = findViewById(R.id.recycler_view_messages)
        messageEditText = findViewById(R.id.edit_text_chat_input)
        sendButton = findViewById(R.id.button_chat_send)

        messageAdapter = MessageAdapter(this@Chat, messageList, this@Chat)
        messageRecyclerView.adapter = messageAdapter
        messageRecyclerView.layoutManager = LinearLayoutManager(this)

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                //Message
                chatViewModel!!.messageList.add(Message(message, "You",true, 0, 1))
                //Preloader
                val preloader = Message(message, "You",true, 0, 2)
                chatViewModel!!.messageList.add(preloader)
                val pos = chatViewModel!!.messageList.size - 1
                messageAdapter.notifyDataSetChanged()
                messageRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                messageEditText.setText("")
                sendMessage(message, preloader)
            }
        }

        //getModelsFromOpenAiApi()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)

        return true
    }


    private fun sendMessage(message: String, mess: Message) {
        val client = OkHttpClient()

        val key = getTokenFromSharedPreferences(this@Chat)

        val model = getModelFromSharedPreferences(this@Chat)
        val request = Request.Builder()
            .url("https://api.openai.com/v1/engines/$model/completions")
            .addHeader("Content-Type", "application/json")

            .addHeader("Authorization", "Bearer $key")
            .post(
                RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    """
                        {
                            "prompt": "$message",
                            "temperature": 0.5,
                            "max_tokens": 1000,
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
                    val pos = chatViewModel.messageList.indexOf(mess)
                    chatViewModel.messageList.set(pos, Message(message, "You",true, 0, 0))
                    messageAdapter.notifyDataSetChanged()
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
                    chatViewModel.messageList.remove(mess)
                    chatViewModel.messageList.add(Message(text, "Gpt", false, totalTokens, 1))
                    messageAdapter.notifyDataSetChanged()
                    messageRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        })
    }

    fun getTokenFromSharedPreferences(context: Context): String? {
        val prefs = context.getSharedPreferences(Companion.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(Companion.TOKEN_KEY, null)
    }

    fun getModelFromSharedPreferences(context: Context): String? {
        val prefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        return prefs.getString("model", null)
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
            R.id.action_exit -> {
                removeTokenFromSharedPreferences(this@Chat)
                // Здесь происходит переход на другую активность при нажатии на кнопку
                val intent = Intent(this@Chat, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                true
            }

            R.id.action_model_select -> {
                val intent = Intent(this@Chat, SelectModel::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

                intent.putExtra("models", models)

                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun replyMessage(message: Message, position: Int) {
        val newMessage = Message(message.messageText, "You",true, 0, 1)
        //Message
        chatViewModel!!.messageList.add(newMessage)
        //Preloader
        val preloader = Message(message.messageText, "You",true, 0, 2)
        chatViewModel!!.messageList.add(preloader)
        sendMessage(newMessage.messageText, preloader)
    }

}

data class Model(
    val name: String,
    val description: String
)



