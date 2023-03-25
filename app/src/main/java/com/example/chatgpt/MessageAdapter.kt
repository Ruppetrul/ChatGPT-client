package com.example.chatgpt

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView


class MessageAdapter(val context: Context, private val messages: MutableList<Message>, val activity: Chat) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_LOADING = 2
    private val VIEW_TYPE_SUCCESS = 1
    private val VIEW_TYPE_FAILURE = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SUCCESS) {
            val layoutRes = R.layout.item_message
            val itemView = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
            return MessageViewHolder(itemView)
        } else if (viewType == VIEW_TYPE_LOADING) {
            val layoutRes = R.layout.item_loading
            val itemView = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
            return LoadingViewHolder(itemView)
        } else {
            val layoutRes = R.layout.item_failure_message
            val itemView = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
            return ButtonViewHolder(itemView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = messages[position]
        return if (message.type == 1) {
            VIEW_TYPE_SUCCESS
        } else if (message.type == 0) {
            VIEW_TYPE_FAILURE
        } else {
            VIEW_TYPE_LOADING
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.textView_message_text)
        val messageSender: TextView = itemView.findViewById(R.id.textView_message_user)
        val tokensCount: TextView = itemView.findViewById(R.id.tv_tokens_count)

        fun bind(message: Message) {
            Log.d("MessageViewHolder", "bind")
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

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message, position: Int) {
            val btn = itemView.findViewById<Button>(R.id.fail_button)
            btn.setOnClickListener {
                messages.remove(message)
                notifyDataSetChanged()
                activity.replyMessage(message, position)
            }
        }
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message, position: Int) {

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val type = getItemViewType(position)

        if (type == VIEW_TYPE_SUCCESS) {
            val messageViewHolder = holder as MessageViewHolder
            messageViewHolder.bind(message)
        } else if (type == VIEW_TYPE_LOADING) {
            val messageViewHolder = holder as LoadingViewHolder
            messageViewHolder.bind(message, position)
        } else {
            val messageViewHolder = holder as ButtonViewHolder
            messageViewHolder.bind(message, position)
        }
    }
}
