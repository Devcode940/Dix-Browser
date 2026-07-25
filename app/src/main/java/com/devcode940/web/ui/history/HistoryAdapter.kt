package com.devcode940.web.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val onItemClick: (HistoryItem) -> Unit,
    private val onDeleteClick: (HistoryItem) -> Unit
) : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryItem) {
            binding.tvTitle.text = item.title
            binding.tvUrl.text = item.url

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem) = oldItem == newItem
    }
}