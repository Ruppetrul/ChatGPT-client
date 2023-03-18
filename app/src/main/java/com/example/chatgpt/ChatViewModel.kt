package com.example.chatgpt

import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {
    var messageList: MutableList<Message> = mutableListOf()
}