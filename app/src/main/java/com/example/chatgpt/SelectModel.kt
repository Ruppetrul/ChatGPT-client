package com.example.chatgpt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SelectModel : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_model)

        val models = intent.getStringArrayExtra("models") as Array<String>

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MyAdapter(models)
        adapter.setOnItemClickListener { position ->
            val selectedData = models[position]
            saveTokenToSharedPreferences(this@SelectModel, selectedData)

            val intent = Intent(this, Chat::class.java)

            intent.putExtra("models", models)

            startActivity(intent)
            finish()
        }
        recyclerView.adapter = adapter
    }

    fun saveTokenToSharedPreferences(context: Context, model: String) {
        val prefs = context.getSharedPreferences( "myPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("model", model).apply()
    }

    // Возвращает список элементов для RecyclerView
    private fun getItems(): List<String> {
        return listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
    }


    // Адаптер для RecyclerView
    private class MyAdapter(private val items: Array<String>) : RecyclerView.Adapter<MyViewHolder>() {

        private var onItemClickListener: ((Int) -> Unit)? = null

        fun setOnItemClickListener(listener: (Int) -> Unit) {
            onItemClickListener = listener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            val holder = MyViewHolder(itemView)
            itemView.setOnClickListener {
                onItemClickListener?.invoke(holder.adapterPosition)
            }
            return holder
        }
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    // ViewHolder для RecyclerView
    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(item: String) {
            textView.text = item
        }
    }
}