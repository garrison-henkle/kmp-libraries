package dev.henkle.compose.ktab.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import dev.henkle.compose.ktab.TabManager

val LocalTabManager = compositionLocalOf<TabManager<*>> { error("No TabManager provided to ProvideTabManager!") }

@Composable
internal fun ProvideTabManager(
    manager: TabManager<*>,
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    value = LocalTabManager provides manager,
    content = content,
)
