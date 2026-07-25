package com.devcode940.web.ui.toolbar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Customizable Toolbar Manager - Phase 3
 */
data class ToolbarButton(
    val id: String,
    val label: String,
    var isVisible: Boolean = true,
    var order: Int = 0
)

class CustomToolbarManager {

    private val _buttons = MutableLiveData<List<ToolbarButton>>()
    val buttons: LiveData<List<ToolbarButton>> get() = _buttons

    private val defaultButtons = listOf(
        ToolbarButton("back", "Back", true, 0),
        ToolbarButton("forward", "Forward", true, 1),
        ToolbarButton("refresh", "Refresh", true, 2),
        ToolbarButton("home", "Home", true, 3),
        ToolbarButton("menu", "Menu", true, 4),
        ToolbarButton("desktop", "Desktop Mode", false, 5),
        ToolbarButton("fullscreen", "Full Screen", false, 6),
        ToolbarButton("translate", "Translate", false, 7),
        ToolbarButton("download", "Downloads", true, 8),
        ToolbarButton("history", "History", true, 9)
    )

    init {
        _buttons.value = defaultButtons
    }

    fun toggleButtonVisibility(id: String, visible: Boolean) {
        val current = _buttons.value?.toMutableList() ?: return
        current.find { it.id == id }?.isVisible = visible
        _buttons.value = current
    }

    fun reorderButtons(newOrder: List<ToolbarButton>) {
        _buttons.value = newOrder.sortedBy { it.order }
    }

    fun getVisibleButtons(): List<ToolbarButton> {
        return _buttons.value?.filter { it.isVisible } ?: emptyList()
    }

    fun addCustomButton(label: String, id: String = "custom_${System.currentTimeMillis()}") {
        val current = _buttons.value?.toMutableList() ?: return
        current.add(ToolbarButton(id, label, true, current.size))
        _buttons.value = current
    }

    fun resetToDefault() {
        _buttons.value = defaultButtons.map { it.copy() }
    }
}