package dev.henkle.compose.ktab.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize
import dev.henkle.compose.ktab.TabManager
import dev.henkle.compose.ktab.model.DropZone
import dev.henkle.compose.ktab.model.LeafNode
import dev.henkle.compose.ktab.model.Tab

@Composable
internal fun <T : Tab<T>> LeafNode(
    modifier: Modifier = Modifier,
    node: LeafNode<T>,
    tabBarContent: @Composable (node: LeafNode<T>, manager: TabManager<T>) -> Unit,
    emptyNodeContent: @Composable BoxScope.() -> Unit,
    dropZoneOverlay: @Composable BoxScope.(zone: DropZone) -> Unit,
) {
    @Suppress("UNCHECKED_CAST")
    val tabManager = LocalTabManager.current as TabManager<T>
    Column(modifier = modifier.fillMaxSize()) {
        tabBarContent(node, tabManager)

        Box(
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    tabManager.updateNodePosition(
                        node = node,
                        bounds = Rect(
                            offset = coordinates.positionInWindow(),
                            size = coordinates.size.toSize(),
                        ),
                    )
                },
        ) {
            if (node.tabs.isNotEmpty() && node.selectedIndex < node.tabs.size) {
                node.selectedTab.Content()
            } else {
                emptyNodeContent()
            }

            tabManager.hoveredNode?.also { (hoveredNode, zone) ->
                if (node.id == hoveredNode.id) {
                    dropZoneOverlay(zone)
                }
            }
        }
    }
}
