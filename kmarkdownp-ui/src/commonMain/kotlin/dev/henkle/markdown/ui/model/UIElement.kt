package dev.henkle.markdown.ui.model

import androidx.compose.ui.text.AnnotatedString
import kotlinx.atomicfu.atomic
import androidx.compose.ui.Alignment as CAlignment

sealed class UIElement {
    val id: String = getID()

    sealed class ContainerElement: UIElement() { abstract val elements: List<UIElement> }
    sealed class Header : ContainerElement()
    sealed class SETextHeader : ContainerElement()

    data class Text(val text: AnnotatedString) : UIElement()
    data class Image(val label: AnnotatedString, val url: String, val title: String?) : UIElement()
    data class LinkDefinition(
        val labelRaw: String,
        val label: List<UIElement>,
        val url: String,
        val title: List<UIElement>?,
    ) : UIElement()
    data class H1(override val elements: List<UIElement>) : Header()
    data class H2(override val elements: List<UIElement>) : Header()
    data class H3(override val elements: List<UIElement>) : Header()
    data class H4(override val elements: List<UIElement>) : Header()
    data class H5(override val elements: List<UIElement>) : Header()
    data class H6(override val elements: List<UIElement>) : Header()
    data class SETextH1(override val elements: List<UIElement>) : SETextHeader()
    data class SETextH2(override val elements: List<UIElement>) : SETextHeader()
    data class CodeBlock(val code: String, val language: String?) : UIElement()
    data class MathBlock(val equation: String) : UIElement()
    data object Divider : UIElement()
    data class Blockquote(override val elements: List<UIElement>) : ContainerElement()
    data class LineBreak(val newlineCount: Int) : UIElement()
    sealed class BaseList<T: BaseList.ListItem> : UIElement() {
        abstract val items: List<T>
        interface ListItem {
            val content: List<UIElement>
            val level: Int
        }
    }
    data class BulletedList(override val items: List<Item>) : BaseList<BulletedList.Item>() {
        data class Item(
            override val content: List<UIElement>,
            override val level: Int,
        ) : ListItem
    }
    data class NumberedList(override val items: List<Item>) : BaseList<NumberedList.Item>() {
        data class Item(
            override val content: List<UIElement>,
            override val level: Int,
            val number: Int,
        ) : ListItem
    }
    data class Table(val columns: List<Column>, val cells: List<List<Cell>>, val hasHeader: Boolean) : UIElement() {
        data class Column(val alignment: Alignment)
        enum class Alignment {
            Left,
            Center,
            Right,
            ;

            fun asComposeAlignment(verticalAlignment: CAlignment.Vertical = CAlignment.Top): CAlignment = when (this) {
                Left -> when (verticalAlignment) {
                    CAlignment.Top -> CAlignment.TopStart
                    CAlignment.Bottom -> CAlignment.BottomStart
                    else -> CAlignment.CenterStart
                }
                Center -> when (verticalAlignment) {
                    CAlignment.Top -> CAlignment.TopCenter
                    CAlignment.Bottom -> CAlignment.BottomCenter
                    else -> CAlignment.Center
                }
                Right -> when (verticalAlignment) {
                    CAlignment.Top -> CAlignment.TopEnd
                    CAlignment.Bottom -> CAlignment.BottomEnd
                    else -> CAlignment.CenterEnd
                }
            }
        }
        data class Cell(val content: List<UIElement>)
    }

    companion object {
        internal val nextID = atomic(0)
        private fun getID(): String = nextID.getAndIncrement().toString()
    }
}
