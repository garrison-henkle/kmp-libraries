package dev.henkle.compose.sheet

import androidx.compose.runtime.Composable

data class ScreenSizePx(val width: Float, val height: Float)

@Composable
expect fun getScreenSize(): ScreenSizePx
