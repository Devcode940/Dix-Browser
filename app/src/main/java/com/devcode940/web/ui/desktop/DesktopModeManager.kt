package com.devcode940.web.ui.desktop

/**
 * Manages Desktop Mode (User-Agent switching).
 *
 * Minimal implementation backing BrowserActivity's desktop-mode toggle.
 */
class DesktopModeManager {

    private var desktopMode = false

    fun setDesktopMode(enable: Boolean) {
        desktopMode = enable
    }

    fun isDesktopMode(): Boolean = desktopMode
}
