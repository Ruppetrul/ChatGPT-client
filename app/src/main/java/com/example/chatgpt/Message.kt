package com.example.chatgpt

data class Message(
    val messageText: String,
   // val messageTime: String,
    val senderName: String,
    val isSentByMe: Boolean,
    val tokens: Int
)
