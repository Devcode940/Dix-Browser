package com.devcode940.web.ui.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devcode940.web.databinding.FragmentBrowserBinding

/**
 * Modern Browser Fragment using ViewModel + ViewBinding
 */
class BrowserFragment : Fragment() {

    private var _binding: FragmentBrowserBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        observeViewModel()
    }

    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    viewModel.setLoading(true)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    viewModel.setLoading(false)
                    viewModel.updateUrl(url ?: "")
                    viewModel.setPageTitle(view?.title ?: "")
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentUrl.observe(viewLifecycleOwner) { url ->
            binding.webView.loadUrl(url)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            // Can be connected to address bar progress
        }
    }

    fun loadUrl(url: String) {
        val processedUrl = viewModel.processAndLoadUrl(url)
        binding.webView.loadUrl(processedUrl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): BrowserFragment {
            return BrowserFragment()
        }
    }
}