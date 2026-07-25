package com.devcode940.web.ui.download

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.databinding.ItemDownloadBinding

class DownloadAdapter(
    private val onItemClick: (DownloadItem) -> Unit
) : ListAdapter<DownloadItem, DownloadAdapter.DownloadViewHolder>(DownloadDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val binding = ItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DownloadViewHolder(
        private val binding: ItemDownloadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DownloadItem) {
            binding.apply {
                tvTitle.text = item.title.ifEmpty { item.fileName }
                tvUrl.text = item.url
                tvStatus.text = "${item.status} • ${item.progress}%"

                progressBar.progress = item.progress
                progressBar.visibility = if (item.status == "Downloading") android.view.View.VISIBLE else android.view.View.GONE

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    class DownloadDiffCallback : DiffUtil.ItemCallback<DownloadItem>() {
        override fun areItemsTheSame(oldItem: DownloadItem, newItem: DownloadItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DownloadItem, newItem: DownloadItem): Boolean {
            return oldItem == newItem
        }
    }
}