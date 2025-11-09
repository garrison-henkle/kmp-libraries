package dev.henkle.compose.ktab.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.henkle.compose.ktab.model.DropZone

@Composable
internal fun BoxScope.DropZoneOverlay(zone: DropZone) {
    val overlayModifier = when (zone) {
        DropZone.Top -> Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .align(Alignment.TopCenter)
        DropZone.Bottom -> Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .align(Alignment.BottomCenter)
        DropZone.Left -> Modifier
            .fillMaxWidth(0.5f)
            .fillMaxHeight()
            .align(Alignment.CenterStart)
        DropZone.Right -> Modifier
            .fillMaxWidth(0.5f)
            .fillMaxHeight()
            .align(Alignment.CenterEnd)
        DropZone.Center -> Modifier.fillMaxSize()
    }

    Box(
        modifier = overlayModifier
            .background(color = Color.Blue.copy(alpha = 0.4f))
            .border(width = 2.dp, color = Color.Blue.copy(alpha = 0.8f))
    )
}
