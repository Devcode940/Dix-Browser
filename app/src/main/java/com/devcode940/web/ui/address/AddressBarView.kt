package com.devcode940.web.ui.address

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.devcode940.web.R
import com.devcode940.web.databinding.ViewAddressBarBinding

/**
 * Modern reusable Address Bar component
 */
class AddressBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAddressBarBinding =
        ViewAddressBarBinding.inflate(LayoutInflater.from(context), this, true)

    private var onUrlSubmitListener: ((String) -> Unit)? = null
    private var onClearListener: (() -> Unit)? = null

    private val addressBarManager = AddressBarManager(context)

    init {
        setupViews()
    }

    private fun setupViews() {
        // Text change listener
        binding.editUrl.doAfterTextChanged { text ->
            binding.btnClear.isVisible = !text.isNullOrEmpty()
        }

        // Editor action (Enter / Go)
        binding.editUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                submitUrl()
                true
            } else {
                false
            }
        }

        // Clear button
        binding.btnClear.setOnClickListener {
            binding.editUrl.text?.clear()
            onClearListener?.invoke()
        }
    }

    private fun submitUrl() {
        val input = binding.editUrl.text?.toString()?.trim() ?: return
        if (input.isBlank()) return

        val processedUrl = addressBarManager.processInput(input)
        onUrlSubmitListener?.invoke(processedUrl)

        // Hide keyboard
        binding.editUrl.clearFocus()
    }

    // Public API
    fun setUrl(url: String) {
        binding.editUrl.setText(url)
        addressBarManager.updateCurrentUrl(url)
    }

    fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        addressBarManager.setLoading(isLoading)
    }

    fun setProgress(progress: Int) {
        binding.progressBar.progress = progress
    }

    fun setOnUrlSubmitListener(listener: (String) -> Unit) {
        onUrlSubmitListener = listener
    }

    fun setOnClearListener(listener: () -> Unit) {
        onClearListener = listener
    }

    fun setSearchEngine(engine: AddressBarManager.SearchEngine) {
        addressBarManager.setSearchEngine(engine)
    }

    fun getCurrentUrl(): String? {
        return addressBarManager.currentUrl.value
    }
}