package dev.henkle.markdown.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.henkle.markdown.ui.MarkdownContent
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle

@Composable
fun MarkdownList(modifier: Modifier = Modifier, element: UIElement.BaseList<*>) {
    val style = LocalMarkdownStyle.current.list
    val getText: (item: UIElement.BaseList.ListItem) -> String = remember(element) {
        when (element) {
            is UIElement.NumberedList -> {
                { item -> "${(item as UIElement.NumberedList.Item).number}${style.numberChar}" }
            }
            is UIElement.BulletedList -> {
                { _ -> style.bulletChar.toString() }
            }
        }
    }
    Column(modifier = modifier) {
        element.items.forEachIndexed { i, item ->
            Row(
                modifier = Modifier.padding(start = style.markerStartMargin),
                verticalAlignment = style.contentAlignment,
            ) {
                Text(
                    modifier = Modifier
                        .align(alignment = style.markerAlignment)
                        .padding(
                            start = style.levelIndentation * item.level,
                            end = style.markerEndMargin,
                        ),
                    text = getText(item),
                    style = style.markerStyle,
                )
                MarkdownContent(elements = item.content)
            }
            if (i != element.items.lastIndex) {
                Spacer(modifier = Modifier.height(height = style.itemSpacing))
            }
        }
    }
}
