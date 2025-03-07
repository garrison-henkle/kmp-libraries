package dev.henkle.markdown.ui.model

import androidx.compose.runtime.Stable

@Stable
sealed class InlineUIElement {
    abstract val id: String

    @Stable
    data class Math(override val id: String, val equation: String) : InlineUIElement()
    @Stable
    data class Link(
        override val id: String,
        val labelRaw: String,
        val label: List<UIElement>,
        val title: List<UIElement>?,
    ) : InlineUIElement()

    @Stable
    data class Code(override val id: String, val code: String) : InlineUIElement()
}
