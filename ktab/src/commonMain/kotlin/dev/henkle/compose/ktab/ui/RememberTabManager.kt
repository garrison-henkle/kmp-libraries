package dev.henkle.compose.ktab.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.henkle.compose.ktab.TabManager
import dev.henkle.compose.ktab.model.Tab

/**
 * Creates and remembers a [TabManager] instance with the provided defaults
 *
 * @param dropZoneThreshold the ratio of the tab's width and height that
 * will be considered part of its drop zones. For example, a value of 0.25
 * means that the left 25% of a tab will be its left split drop zone, the
 * top 25% will be its top split drop zone, etc. Any remaining space will
 * be used for the center drop zone (used to add tabs to a node without
 * splitting it)
 * @param defaultSplitRatio the default ratio used to split the available
 * screen space of a node between its children. For example, a 0.5 ratio
 * will evenly split the available screen space between the two children
 */
@Composable
fun <T: Tab<T>> rememberTabManager(
    dropZoneThreshold: Float = TabManager.DEFAULT_DROP_ZONE_THRESHOLD,
    defaultSplitRatio: Float = TabManager.DEFAULT_SPLIT_RATIO,
): TabManager<T> =
    remember {
        TabManager(
            dropZoneThreshold = dropZoneThreshold,
            defaultSplitRatio = defaultSplitRatio,
        )
    }
