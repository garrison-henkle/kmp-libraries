package dev.henkle.markdown.ui.demo

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

@Composable
actual fun LatexView(
    modifier: Modifier,
    text: String,
    fontSize: TextUnit,
    color: Color,
    textAlignment: TextAlign,
) {
    // TODO(garrison): implement something using Swing interop
    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontSize = fontSize,
        textAlign = textAlignment,
    )
}
