package dev.henkle.markdown.ui.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.Alignment as CAlignment

@Stable
sealed class UIElement {
    abstract val id: String

    // parent types

    @Stable
    sealed class ContainerElement: UIElement() { abstract val elements: List<UIElement> }

    @Stable
    sealed class Header : ContainerElement()

    @Stable
    sealed class SETextHeader : ContainerElement()

    // elements

    @Stable
    data class Text(override val id: String, val text: AnnotatedString) : UIElement()

    @Stable
    data class Image(
        override val id: String,
        val label: AnnotatedString,
        val url: String,
        val title: String?,
    ) : UIElement()

    @Stable
    data class LinkDefinition(
        override val id: String,
        val labelRaw: String,
        val label: List<UIElement>,
        val url: String,
        val title: List<UIElement>?,
    ) : UIElement()

    @Stable
    data class H1(override val id: String, override val elements: List<UIElement>) : Header()

    @Stable
    data class H2(override val id: String, override val elements: List<UIElement>) : Header()

    @Stable
    data class H3(override val id: String, override val elements: List<UIElement>) : Header()

    @Stable
    data class H4(override val id: String, override val elements: List<UIElement>) : Header()

    @Stable
    data class H5(override val id: String, override val elements: List<UIElement>) : Header()

    @Stable
    data class H6(override val id: String, override val elements: List<UIElement>) : Header()

    @Stable
    data class SETextH1(override val id: String, override val elements: List<UIElement>) : SETextHeader()

    @Stable
    data class SETextH2(override val id: String, override val elements: List<UIElement>) : SETextHeader()

    @Stable
    data class CodeBlock(override val id: String, val code: String, val language: String?) : UIElement()

    @Stable
    data class MathBlock(override val id: String, val equation: String) : UIElement()

    @Stable
    data class Divider(override val id: String) : UIElement()

    @Stable
    data class Blockquote(override val id: String, override val elements: List<UIElement>) : ContainerElement()

    @Stable
    data class LineBreak(override val id: String, val newlineCount: Int) : UIElement()

    @Stable
    sealed class BaseList<T: BaseList.ListItem> : UIElement() {
        abstract val items: List<T>
        @Stable
        interface ListItem {
            val content: List<UIElement>
            val level: Int
        }
    }

    @Stable
    data class BulletedList(override val id: String, override val items: List<Item>) : BaseList<BulletedList.Item>() {
        @Stable
        data class Item(
            override val content: List<UIElement>,
            override val level: Int,
        ) : ListItem
    }
    @Stable
    data class NumberedList(override val id: String, override val items: List<Item>) : BaseList<NumberedList.Item>() {
        @Stable
        data class Item(
            override val content: List<UIElement>,
            override val level: Int,
            val number: Int,
        ) : ListItem
    }
    @Stable
    data class Table(
        override val id: String,
        val columns: List<Column>,
        val cells: List<List<Cell>>,
        val hasHeader: Boolean,
    ) : UIElement() {
        @Stable
        data class Column(val alignment: Alignment)
        @Stable
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
        @Stable
        data class Cell(val content: List<UIElement>)
    }
}
