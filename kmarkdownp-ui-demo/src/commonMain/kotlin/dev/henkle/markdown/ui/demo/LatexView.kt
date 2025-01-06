package dev.henkle.markdown.ui.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
expect fun LatexView(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit = 12.sp,
    color: Color = Color.Black,
    textAlignment: TextAlign = TextAlign.Left,
)
