package dev.henkle.markdown.ui.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import com.agog.mathdisplay.MTMathView

@Composable
actual fun LatexView(
    modifier: Modifier,
    text: String,
    fontSize: TextUnit,
    color: Color,
    textAlignment: TextAlign,
) {
    val density = LocalDensity.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MTMathView(context).apply {
                latex = text
                this.fontSize = with(density) { fontSize.toPx() }
                this.textColor = color.toArgb()
                this.textAlignment = when (textAlignment) {
                    TextAlign.Center -> MTMathView.MTTextAlignment.KMTTextAlignmentCenter
                    TextAlign.End,
                    TextAlign.Right -> MTMathView.MTTextAlignment.KMTTextAlignmentRight
                    else -> MTMathView.MTTextAlignment.KMTTextAlignmentLeft
                }
            }
        },
        update = { view ->
            view.latex = text
            view.fontSize = with(density) { fontSize.toPx() }
            view.textColor = color.toArgb()
            view.textAlignment = when (textAlignment) {
                TextAlign.Center -> MTMathView.MTTextAlignment.KMTTextAlignmentCenter
                TextAlign.End,
                TextAlign.Right -> MTMathView.MTTextAlignment.KMTTextAlignmentRight
                else -> MTMathView.MTTextAlignment.KMTTextAlignmentLeft
            }
        },
    )
}
