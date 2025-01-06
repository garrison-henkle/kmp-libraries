package dev.henkle.compose.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenSize(): ScreenSizePx {
    val windowInfo = LocalWindowInfo.current
    return remember(windowInfo) {
        ScreenSizePx(
            width = windowInfo.containerSize.width.toFloat(),
            height = windowInfo.containerSize.height.toFloat(),
        )
    }
}
