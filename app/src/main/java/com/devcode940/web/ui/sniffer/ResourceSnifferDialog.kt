package com.devcode940.web.ui.sniffer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devcode940.web.databinding.DialogResourceSnifferBinding

/**
 * Resource Sniffer Dialog - Phase 3
 * Supports real data from WebView
 */
class ResourceSnifferDialog : DialogFragment() {

    private var _binding: DialogResourceSnifferBinding? = null
    private val binding get() = _binding!!

    private var resources: List<ResourceItem> = emptyList()
    private lateinit var adapter: ResourceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogResourceSnifferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        adapter.submitList(resources)

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnDownloadAll.setOnClickListener { downloadAll() }
    }

    private fun setupRecyclerView() {
        adapter = ResourceAdapter { resource ->
            downloadResource(resource)
        }

        binding.recyclerResources.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@ResourceSnifferDialog.adapter
        }
    }

    private fun downloadResource(resource: ResourceItem) {
        android.widget.Toast.makeText(requireContext(), "Downloading: ${resource.title}", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun downloadAll() {
        resources.forEach { downloadResource(it) }
        dismiss()
    }

    fun setResources(newResources: List<ResourceItem>) {
        this.resources = newResources
        if (::adapter.isInitialized) {
            adapter.submitList(newResources)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(resources: List<ResourceItem> = emptyList()): ResourceSnifferDialog {
            return ResourceSnifferDialog().apply {
                this.resources = resources
            }
        }
    }
}