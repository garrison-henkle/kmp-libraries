package dev.henkle.markdown.ui.utils

enum class Renderer {
    AndroidCanvas,
    Skiko,
}

/**
 * Retrieves the current renderer for this device
 */
expect fun getRenderer(): Renderer
