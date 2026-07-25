package com.devcode940.web.ui.toolbar

import androidx.lifecycle.ViewModel

class ToolbarConfigViewModel : ViewModel() {

    private val toolbarManager = CustomToolbarManager()

    val buttons = toolbarManager.buttons

    fun toggleButtonVisibility(id: String, visible: Boolean) {
        toolbarManager.toggleButtonVisibility(id, visible)
    }

    fun moveButton(from: Int, to: Int) {
        val currentList = toolbarManager.buttons.value?.toMutableList() ?: return
        if (from < 0 || to < 0 || from >= currentList.size || to >= currentList.size) return

        val item = currentList.removeAt(from)
        currentList.add(to, item)

        // Update order
        currentList.forEachIndexed { index, button ->
            button.order = index
        }

        toolbarManager.reorderButtons(currentList)
    }

    fun resetToDefault() {
        toolbarManager.resetToDefault()
    }

    fun addCustomButton(label: String) {
        toolbarManager.addCustomButton(label)
    }
}