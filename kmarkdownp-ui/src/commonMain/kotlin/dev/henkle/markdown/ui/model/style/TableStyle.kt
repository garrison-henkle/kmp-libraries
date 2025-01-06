package dev.henkle.markdown.ui.model.style

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.henkle.markdown.ui.model.Padding

data class TableStyle(
    val textStyle: TextStyle,
    val imageStyle: ImageStyle,
    val cellMeasurementPadding: (x: Int, y: Int) -> Padding,
    val cellModifier: (x: Int, y: Int, width: Int, height: Int, tableHasHeader: Boolean) -> Modifier,
) : Style
