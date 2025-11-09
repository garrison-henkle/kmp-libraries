package dev.henkle.compose.ktab.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.henkle.compose.ktab.TabManager
import dev.henkle.compose.ktab.model.DropZone
import dev.henkle.compose.ktab.model.LeafNode
import dev.henkle.compose.ktab.model.SplitNode
import dev.henkle.compose.ktab.model.SplitOrientation
import dev.henkle.compose.ktab.model.Tab

@Composable
internal fun <T : Tab<T>> SplitNode(
    node: SplitNode<T>,
    tabBarContent: @Composable (node: LeafNode<T>, manager: TabManager<T>) -> Unit,
    emptyNodeContent: @Composable BoxScope.() -> Unit,
    dropZoneOverlay: @Composable BoxScope.(zone: DropZone) -> Unit,
) {
    if (node.orientation == SplitOrientation.Horizontal) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(weight = node.splitRatio)) {
                Node(
                    node = node.first,
                    tabBarContent = tabBarContent,
                    emptyNodeContent = emptyNodeContent,
                    dropZoneOverlay = dropZoneOverlay,
                )
            }
            Divider(modifier = Modifier.width(width = 2.dp).fillMaxHeight())
            Box(modifier = Modifier.weight(weight = 1f - node.splitRatio)) {
                Node(
                    node = node.second,
                    tabBarContent = tabBarContent,
                    emptyNodeContent = emptyNodeContent,
                    dropZoneOverlay = dropZoneOverlay,
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(weight = node.splitRatio)) {
                Node(
                    node = node.first,
                    tabBarContent = tabBarContent,
                    emptyNodeContent = emptyNodeContent,
                    dropZoneOverlay = dropZoneOverlay,
                )
            }
            Divider(modifier = Modifier.height(height = 2.dp).fillMaxWidth())
            Box(modifier = Modifier.weight(weight = 1f - node.splitRatio)) {
                Node(
                    node = node.second,
                    tabBarContent = tabBarContent,
                    emptyNodeContent = emptyNodeContent,
                    dropZoneOverlay = dropZoneOverlay,
                )
            }
        }
    }
}
