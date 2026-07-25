package com.devcode940.web.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devcode940.web.databinding.FragmentDownloadBinding

/**
 * Download Manager UI Fragment
 */
class DownloadFragment : Fragment() {

    private var _binding: FragmentDownloadBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DownloadViewModel by viewModels()
    private lateinit var adapter: DownloadAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeDownloads()
    }

    private fun setupRecyclerView() {
        adapter = DownloadAdapter { downloadItem ->
            // Handle item click (open file)
            viewModel.openDownload(requireContext(), downloadItem)
        }

        binding.recyclerDownloads.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DownloadFragment.adapter
        }

        binding.btnClearAll.setOnClickListener {
            viewModel.clearAllDownloads()
        }
    }

    private fun observeDownloads() {
        viewModel.downloads.observe(viewLifecycleOwner) { downloads ->
            adapter.submitList(downloads)
            binding.emptyView.visibility = if (downloads.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = DownloadFragment()
    }
}