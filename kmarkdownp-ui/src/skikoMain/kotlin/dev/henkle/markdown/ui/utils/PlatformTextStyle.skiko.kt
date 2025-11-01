package dev.henkle.markdown.ui.utils

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle

@OptIn(ExperimentalTextApi::class)
actual fun getPlatformTextStyle(): PlatformTextStyle = PlatformTextStyle(textDecorationLineStyle = null)
