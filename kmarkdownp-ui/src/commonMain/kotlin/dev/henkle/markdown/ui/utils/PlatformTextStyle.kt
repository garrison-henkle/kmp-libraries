package dev.henkle.markdown.ui.utils

import androidx.compose.ui.text.PlatformTextStyle

/**
 * Retrieves a default [PlatformTextStyle] instance for the [dev.henkle.markdown.ui.components.MarkdownText] component
 */
expect fun getPlatformTextStyle(): PlatformTextStyle
