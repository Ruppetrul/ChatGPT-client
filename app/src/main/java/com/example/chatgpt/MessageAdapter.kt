package com.example.chatgpt

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
class MessageAdapter(val context: Context, private val messages: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]

        holder.bind(context, message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.textView_message_text)
        val messageSender: TextView = itemView.findViewById(R.id.textView_message_user)
        val tokensCount: TextView = itemView.findViewById(R.id.tv_tokens_count)

        fun bind(context: Context, message: Message) {
            messageText.text = message.messageText
            messageSender.text = message.senderName
            if (message.tokens > 0) {
                tokensCount.text = "Tokens count:"  + message.tokens.toString()
            } else {
                tokensCount.text = ""
            }

            if (!message.isSentByMe) {
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.message_background))
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            }
        }
    }
}
