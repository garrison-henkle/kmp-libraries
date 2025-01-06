package dev.henkle.markdown.ui.model

sealed class InlineUIElement {
    val id: String = getID()

    data class Math(val equation: String) : InlineUIElement()
    data class Link(val labelRaw: String, val label: List<UIElement>, val title: List<UIElement>?) : InlineUIElement()
    data class Code(val code: String) : InlineUIElement()

    companion object {
        private fun getID(): String = UIElement.nextID.getAndIncrement().toString()
    }
}
