package dev.henkle.compose.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
actual fun getScreenSize(): ScreenSizePx {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    return remember(configuration, density) {
        with(density) {
            ScreenSizePx(
                width = configuration.screenWidthDp.dp.toPx(),
                height = configuration.screenHeightDp.dp.toPx(),
            )
        }
    }
}
