package dev.henkle.compose.ktab.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import dev.henkle.compose.ktab.TabManager
import dev.henkle.compose.ktab.model.DropZone
import dev.henkle.compose.ktab.model.LeafNode
import dev.henkle.compose.ktab.model.PaneNode
import dev.henkle.compose.ktab.model.SplitNode
import dev.henkle.compose.ktab.model.Tab

@Composable
internal fun <T : Tab<T>> Node(
    node: PaneNode<T>,
    tabBarContent: @Composable (node: LeafNode<T>, manager: TabManager<T>) -> Unit,
    emptyNodeContent: @Composable BoxScope.() -> Unit,
    dropZoneOverlay: @Composable BoxScope.(zone: DropZone) -> Unit,
) {
    when (node) {
        is LeafNode ->
            LeafNode(
                node = node,
                tabBarContent = tabBarContent,
                emptyNodeContent = emptyNodeContent,
                dropZoneOverlay = dropZoneOverlay,
            )
        is SplitNode ->
            SplitNode(
                node = node,
                tabBarContent = tabBarContent,
                emptyNodeContent = emptyNodeContent,
                dropZoneOverlay = dropZoneOverlay,
            )
    }
}