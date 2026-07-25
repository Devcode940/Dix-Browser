package com.devcode940.web.ui.sniffer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.databinding.ItemResourceBinding

data class ResourceItem(
    val title: String,
    val url: String,
    val mimeType: String
)

class ResourceAdapter(
    private val onDownloadClick: (ResourceItem) -> Unit
) : ListAdapter<ResourceItem, ResourceAdapter.ResourceViewHolder>(ResourceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val binding = ItemResourceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ResourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ResourceViewHolder(private val binding: ItemResourceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ResourceItem) {
            binding.tvTitle.text = item.title
            binding.tvType.text = item.mimeType

            binding.btnDownload.setOnClickListener {
                onDownloadClick(item)
            }
        }
    }

    class ResourceDiffCallback : DiffUtil.ItemCallback<ResourceItem>() {
        override fun areItemsTheSame(oldItem: ResourceItem, newItem: ResourceItem) = oldItem.url == newItem.url
        override fun areContentsTheSame(oldItem: ResourceItem, newItem: ResourceItem) = oldItem == newItem
    }
}