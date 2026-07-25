package com.devcode940.web.ui.summarizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.devcode940.web.databinding.FragmentSummarizerBinding

/**
 * Advanced Summarizer Fragment
 * Integrates Arena.ai as an in-app summarizer
 */
class SummarizerFragment : Fragment() {

    private var _binding: FragmentSummarizerBinding? = null
    private val binding get() = _binding!!

    private var currentUrl: String? = null
    private var selectedText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentUrl = it.getString(ARG_URL)
            selectedText = it.getString(ARG_SELECTED_TEXT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummarizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView()
        setupUI()
    }

    private fun setupWebView() {
        binding.webViewSummarizer.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()

            // Load Arena.ai summarizer
            loadUrl("https://arena.ai")
        }
    }

    private fun setupUI() {
        // Show current page info
        binding.tvCurrentUrl.text = currentUrl ?: "No URL"

        // Summarize Current Page button
        binding.btnSummarizePage.setOnClickListener {
            currentUrl?.let { url ->
                summarizePage(url)
            }
        }

        // Summarize Selected Text button
        binding.btnSummarizeText.setOnClickListener {
            selectedText?.let { text ->
                summarizeText(text)
            } ?: run {
                binding.tvStatus.text = "No text selected"
            }
        }

        // Close button
        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun summarizePage(url: String) {
        binding.tvStatus.text = "Sending page to Arena.ai..."

        // In a real implementation, we would use Arena.ai's API or deep link.
        // For now, we load Arena.ai and inject the URL via JavaScript if possible.
        binding.webViewSummarizer.evaluateJavascript(
            """
                (function() {
                    // Try to find input field in Arena.ai and fill URL
                    var input = document.querySelector('input[type="text"], textarea');
                    if (input) {
                        input.value = '$url';
                        input.dispatchEvent(new Event('input', { bubbles: true }));
                    }
                })();
            """.trimIndent()
        ) {
            binding.tvStatus.text = "Page sent to summarizer"
        }
    }

    private fun summarizeText(text: String) {
        binding.tvStatus.text = "Sending selected text..."

        binding.webViewSummarizer.evaluateJavascript(
            """
                (function() {
                    var input = document.querySelector('input[type="text"], textarea');
                    if (input) {
                        input.value = 'Summarize this: $text';
                        input.dispatchEvent(new Event('input', { bubbles: true }));
                    }
                })();
            """.trimIndent()
        ) {
            binding.tvStatus.text = "Text sent to summarizer"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_URL = "arg_url"
        private const val ARG_SELECTED_TEXT = "arg_selected_text"

        fun newInstance(url: String? = null, selectedText: String? = null): SummarizerFragment {
            return SummarizerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                    putString(ARG_SELECTED_TEXT, selectedText)
                }
            }
        }
    }
}