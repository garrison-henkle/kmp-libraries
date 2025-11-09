package dev.henkle.compose.ktab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.henkle.compose.ktab.TabManager
import dev.henkle.compose.ktab.model.DropZone
import dev.henkle.compose.ktab.model.LeafNode
import dev.henkle.compose.ktab.model.Tab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * A split-screen enabled tab layout. Displayed tabs
 * can be drag-and-dropped onto each other to create
 * splits, and splits can be dragged to control the
 * ratio of the available screen space distributed each
 * split screen tab.
 *
 * Use [rememberTabManager] to get a [TabManager] instance.
 *
 * @param modifier a collection of [Modifier] elements to
 * apply to the root of this layout
 * @param manager the [TabManager] instance that will
 * orchestrate the addition, removal, and display of the tabs
 * @param tabBar the composable that draws the tab bar for a
 * split screen node
 * @param emptyRootNodeContent the content to show in an empty
 * split screen node. In practice, only the root node can
 * ever be empty, as empty non-root nodes are collapsed.
 * @param dropZoneOverlay an overlay shown above a tab when it
 * is under a dragged tab that indicates where the tab will be
 * inserted into the node
 */
@Composable
fun <T : Tab<T>> TabLayout(
    modifier: Modifier = Modifier,
    manager: TabManager<T>,
    draggedTabPreview: @Composable (offset: Offset, tab: T) -> Unit,
    tabBar: @Composable (node: LeafNode<T>, manager: TabManager<T>) -> Unit,
    emptyRootNodeContent: @Composable BoxScope.() -> Unit,
    dropZoneOverlay: @Composable BoxScope.(zone: DropZone) -> Unit,
) {
    LaunchedEffect(key1 = manager) {
        withContext(context = Dispatchers.IO) {
            manager.observeDrags()
        }
    }

    ProvideTabManager(manager = manager) {
        Box(modifier = modifier.fillMaxSize()) {
            Node(
                node = manager.root,
                tabBarContent = tabBar,
                emptyNodeContent = emptyRootNodeContent,
                dropZoneOverlay = dropZoneOverlay,
            )

            manager.draggedTab?.let { draggedTab ->
                manager.dragPosition?.let { position ->
                    draggedTabPreview(position, draggedTab.tab)
                }
            }
        }
    }
}
