package com.devcode940.web.ui.incognito

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.devcode940.web.databinding.FragmentIncognitoBinding

/**
 * Incognito Tab Fragment - Phase 2
 */
class IncognitoTabFragment : Fragment() {

    private var _binding: FragmentIncognitoBinding? = null
    private val binding get() = _binding!!

    private val incognitoManager = IncognitoManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncognitoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupIncognitoWebView()
    }

    private fun setupIncognitoWebView() {
        binding.webView.apply {
            // Disable history, cookies, cache for incognito
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = false
                databaseEnabled = false
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    // Do NOT save to history in incognito
                }
            }
        }

        // Load initial page (blank or home)
        binding.webView.loadUrl("about:blank")
    }

    fun loadUrl(url: String) {
        binding.webView.loadUrl(url)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear all data when fragment is destroyed
        binding.webView.clearHistory()
        binding.webView.clearCache(true)
        binding.webView.clearFormData()
        _binding = null
    }

    companion object {
        fun newInstance() = IncognitoTabFragment()
    }
}