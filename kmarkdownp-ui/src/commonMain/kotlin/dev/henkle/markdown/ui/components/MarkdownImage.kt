package dev.henkle.markdown.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle

@Composable
fun MarkdownImage(modifier: Modifier = Modifier, element: UIElement.Image) {
    val style = LocalMarkdownStyle.current.image
    LaunchedEffect(Unit) {

    }
    AsyncImage(
        modifier = modifier.run {
            var newModifier = this
            newModifier = when (style.width) {
                Dp.Unspecified -> newModifier
                Dp.Infinity -> newModifier.fillMaxWidth()
                else -> newModifier.width(width = style.width)
            }
            newModifier = when (style.height) {
                Dp.Unspecified -> newModifier
                Dp.Infinity -> newModifier.fillMaxHeight()
                else -> newModifier.height(height = style.height)
            }
            newModifier
        },
        model = element.url,
        contentDescription = element.title ?: element.label.text,
        contentScale = style.contentScale,
        alignment = style.alignment,
        onError = {
            Logger.e("kmarkdownp") {
                "image loading error: $it"
            }
        }
    )
}
