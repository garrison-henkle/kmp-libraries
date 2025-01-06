package dev.henkle.markdown.ui.demo

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LatexView(
    modifier: Modifier,
    text: String,
    fontSize: TextUnit,
    color: Color,
    textAlignment: TextAlign,
) {
    val factory = LocalLatexWrapperFactory.current
    var wrapper by remember(Unit) { mutableStateOf<LatexWrapper?>(null) }
    var width by remember { mutableStateOf(0.dp) }
    var height by remember { mutableStateOf(0.dp) }
    val view = remember(factory) {
        val latexWrapper = factory.create(
            text = text,
            fontSizePt = fontSize.value.toDouble(),
            textColor = UIColor(
                red = color.red.toDouble(),
                green = color.green.toDouble(),
                blue = color.blue.toDouble(),
                alpha = color.alpha.toDouble(),
            ),
            textAlignment = when (textAlignment) {
                TextAlign.Center -> IOSTextAlignment.Center
                TextAlign.Right,
                TextAlign.End -> IOSTextAlignment.Right
                else -> IOSTextAlignment.Left
            },
            onSizeChanged = { widthPt, heightPt ->
                Logger.e("garrison") { "onSizeChanged: width=$widthPt, height=$heightPt" }
                val widthDp = widthPt.dp
                val heightDp = heightPt.dp
                if (widthDp != width) width = widthDp
                if (heightDp != height) height = heightDp
            }
        )
        wrapper = latexWrapper
        latexWrapper.view
    }
    UIKitView(
        modifier = modifier
            .then(if (width == 0.dp) Modifier else Modifier.width(width = width))
            .then(if (height == 0.dp) Modifier else Modifier.height(height = height)),
        factory = {
            Logger.e("garrison") { "UIKitView factory ran" }
            view
        },
        update = {
            Logger.e("garrison") { "UIKitView update ran for '$text'" }
            wrapper?.update(
                text = text,
                fontSizePt = fontSize.value.toDouble(),
                textColor = UIColor(
                    red = color.red.toDouble(),
                    green = color.green.toDouble(),
                    blue = color.blue.toDouble(),
                    alpha = color.alpha.toDouble(),
                ),
                textAlignment = when (textAlignment) {
                    TextAlign.Center -> IOSTextAlignment.Center
                    TextAlign.Right,
                    TextAlign.End -> IOSTextAlignment.Right
                    else -> IOSTextAlignment.Left
                }
            )
        },
        interactive = false,
    )
}
