package com.devcode940.web

import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView

/**
 * Handles WebView errors with a custom error view
 */
class WebViewErrorHandler(
    private val errorView: View,
    private val onRetry: () -> Unit
) : WebViewClient() {

    private val tvErrorMessage: TextView? = errorView.findViewById(R.id.tv_error_message)
    private val btnRetry: Button? = errorView.findViewById(R.id.btn_retry)

    init {
        btnRetry?.setOnClickListener {
            errorView.visibility = View.GONE
            onRetry()
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        if (request?.isForMainFrame == true) {
            showErrorPage(error?.description?.toString() ?: "Unknown error")
        }
    }

    private fun showErrorPage(message: String) {
        errorView.visibility = View.VISIBLE
        tvErrorMessage?.text = message
    }
}