package com.devcode940.web.ui.toolbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.databinding.FragmentToolbarConfigBinding

/**
 * Toolbar Customization Screen with Drag-to-Reorder - Phase 3
 */
class ToolbarConfigFragment : Fragment() {

    private var _binding: FragmentToolbarConfigBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ToolbarConfigViewModel by viewModels()
    private lateinit var adapter: ToolbarButtonAdapter

    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolbarConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeButtons()

        binding.btnReset.setOnClickListener {
            viewModel.resetToDefault()
        }
    }

    private fun setupRecyclerView() {
        adapter = ToolbarButtonAdapter(
            onVisibilityChanged = { button, isChecked ->
                viewModel.toggleButtonVisibility(button.id, isChecked)
            },
            onStartDrag = { viewHolder ->
                itemTouchHelper.startDrag(viewHolder)
            }
        )

        binding.recyclerToolbarButtons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@ToolbarConfigFragment.adapter
        }

        // Setup drag-to-reorder
        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition

                viewModel.moveButton(fromPos, toPos)
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerToolbarButtons)
    }

    private fun observeButtons() {
        viewModel.buttons.observe(viewLifecycleOwner) { buttons ->
            adapter.submitList(buttons)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}