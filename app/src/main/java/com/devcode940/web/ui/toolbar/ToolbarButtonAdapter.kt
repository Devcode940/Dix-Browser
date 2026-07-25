package com.devcode940.web.ui.toolbar

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.databinding.ItemToolbarButtonBinding

class ToolbarButtonAdapter(
    private val onVisibilityChanged: (ToolbarButton, Boolean) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit
) : ListAdapter<ToolbarButton, ToolbarButtonAdapter.ButtonViewHolder>(ButtonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val binding = ItemToolbarButtonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ButtonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ButtonViewHolder(private val binding: ItemToolbarButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(button: ToolbarButton) {
            binding.tvButtonLabel.text = button.label
            binding.switchVisible.isChecked = button.isVisible

            binding.switchVisible.setOnCheckedChangeListener { _, isChecked ->
                onVisibilityChanged(button, isChecked)
            }

            // Drag handle
            binding.dragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    onStartDrag(this)
                }
                false
            }
        }
    }

    class ButtonDiffCallback : DiffUtil.ItemCallback<ToolbarButton>() {
        override fun areItemsTheSame(oldItem: ToolbarButton, newItem: ToolbarButton) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ToolbarButton, newItem: ToolbarButton) = oldItem == newItem
    }
}