package com.devcode940.web.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devcode940.web.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeHistory()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onItemClick = { item ->
                // TODO: Load URL in browser
            },
            onDeleteClick = { item ->
                viewModel.removeHistory(item)
            }
        )

        binding.recyclerHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@HistoryFragment.adapter
        }

        binding.btnClearAll.setOnClickListener {
            viewModel.clearAll()
        }
    }

    private fun observeHistory() {
        viewModel.history.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HistoryFragment()
    }
}