package dev.henkle.markdown.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

fun Modifier.bottomBorder(color: Color = Color.Black) = then(
    Modifier.drawBehind {
        val y = size.height - 1f

        drawLine(
            color = color,
            start = Offset(x = 0f, y = y),
            end = Offset(x = size.width , y = y),
            strokeWidth = 1f,
        )
    }
)

fun Modifier.topBorder(color: Color = Color.Black) = then(
    Modifier.drawBehind {
        val y = 1f

        drawLine(
            color = color,
            start = Offset(x = 0f, y = y),
            end = Offset(x = size.width , y = y),
            strokeWidth = 1f,
        )
    }
)

fun Modifier.leftBorder(color: Color = Color.Black) = then(
    Modifier.drawBehind {
        val x = 1f

        drawLine(
            color = color,
            start = Offset(x = x, y = 0f),
            end = Offset(x = x , y = size.height),
            strokeWidth = 1f,
        )
    }
)

fun Modifier.rightBorder(color: Color = Color.Black) = then(
    Modifier.drawBehind {
        val x = size.width - 1f

        drawLine(
            color = color,
            start = Offset(x = x, y = 0f),
            end = Offset(x = x, y = size.height),
            strokeWidth = 1f,
        )
    }
)