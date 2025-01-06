package dev.henkle.compose.sheet

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

internal object BetterBottomSheetDefaults {
    const val SNAP_SCREEN_PERCENT = 0.5f
    val velocityThresholdDp = 8_000.dp
    val scrimColor = Color.Black.copy(alpha = 0.4f)

    fun velocityThreshold(density: Density): () -> Float = {
        with(density) { velocityThresholdDp.toPx() }
    }
}