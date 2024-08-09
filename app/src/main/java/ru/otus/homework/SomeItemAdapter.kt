package ru.otus.homework

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SomeItemAdapter : ListAdapter<SomeItem, SomeItemAdapter.Holder>(SomeItemsDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        LayoutInflater.from(parent.context).inflate(R.layout.vh_item, parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val word: TextView = itemView as TextView

        fun bind(item: SomeItem) {
            word.text = item.word
        }
    }
}

object SomeItemsDiffCallback : DiffUtil.ItemCallback<SomeItem>() {
    override fun areItemsTheSame(oldItem: SomeItem, newItem: SomeItem): Boolean {
        return oldItem.word == newItem.word
    }

    override fun areContentsTheSame(oldItem: SomeItem, newItem: SomeItem): Boolean {
        return oldItem == newItem
    }
}