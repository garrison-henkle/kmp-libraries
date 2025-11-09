@file:OptIn(ExperimentalAtomicApi::class)
@file:Suppress("UNCHECKED_CAST")

package dev.henkle.compose.ktab.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.henkle.compose.ktab.TabManager
import dev.henkle.compose.ktab.model.*
import dev.henkle.compose.ktab.model.TabCompanion.DeserializeResult
import dev.henkle.compose.ktab.ui.TabLayout
import dev.henkle.compose.ktab.ui.rememberTabManager
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

@Composable
fun App() {
    val manager = rememberTabManager<TabImpl>()
    TabLayout(
        modifier = Modifier.fillMaxSize(),
        manager = manager,
        tabBar = ::TabBar,
        emptyRootNodeContent = {
            Text(
                modifier = Modifier.align(alignment = Alignment.Center),
                text = "Click the plus button in the tab bar to add your first tab!",
            )
        },
        dropZoneOverlay = { zone -> DropZoneOverlay(zone = zone) },
        draggedTabPreview = { offset, tab ->
            val measurer = rememberTextMeasurer()
            val style = remember {
                TextStyle(fontSize = 13.sp, color = Color.White)
            }
            val (titleWidth, titleHeight) = remember(key1 = tab.title, key2 = measurer) {
                measurer.measure(text = tab.title, style = style).size.let { (width, height) ->
                    width.toFloat() to height.toFloat()
                }
            }
            Box(
                modifier = Modifier
                    .offset(
                        x = (offset.x / 2 - titleWidth / 2).dp - 6.dp,
                        y = (offset.y / 2 - titleHeight / 2).dp - 4.dp,
                    )
                    .background(Color(0xCC3C3F41))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = tab.title,
                    style = style,
                )
            }
        }
    )
}

@Composable
internal fun TabBar(
    node: LeafNode<TabImpl>,
    manager: TabManager<TabImpl>,
) {
    val tabShape = remember {
        RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 40.dp)
            .background(color = Color(color = 0xffeaeaea)),
    ) {
        node.tabs.forEachIndexed { index, tab ->
            Tab(
                tab = tab,
                node = node,
                shape = tabShape,
                isDragging = manager.isDragging,
                isSelected = index == node.selectedIndex,
                startDrag = { offset ->
                    manager.startDrag(
                        sourceNode = node,
                        tab = tab,
                        initialOffset = offset,
                    )
                },
                updateDragPosition = manager::updateDragPosition,
                endDrag = manager::endDrag,
                onSelect = { node.selectTab(index = index) },
                onClose = { manager.closeTab(node = node, tabId = tab.id) },
            )
        }

        Box(
            modifier = Modifier
                .width(width = 40.dp)
                .clip(shape = tabShape)
                .fillMaxHeight()
                .clickable {
                    manager.addTab(
                        node = node,
                        tab = TabImpl(),
                    )
                }
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.Add,
                contentDescription = "Add Tab",
                tint = Color.Gray,
            )
        }
    }
}

@Composable
internal fun Tab(
    tab: TabImpl,
    node: LeafNode<TabImpl>,
    shape: Shape,
    isSelected: Boolean,
    isDragging: Boolean,
    startDrag: (offset: Offset) -> Unit,
    updateDragPosition: (offset: Offset) -> Unit,
    endDrag: () -> Unit,
    onSelect: () -> Unit,
    onClose: () -> Unit,
) {
    var tabPosition by remember { mutableStateOf(value = Offset.Zero) }

    Row(
        modifier = Modifier
            .height(height = 40.dp)
            .clip(shape = shape)
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.33f),
                shape = shape,
            ).background(color = if (isSelected) Color(0xFFaeaeae) else Color.Transparent)
            .onGloballyPositioned { coordinates ->
                tabPosition = coordinates.positionInWindow()
            }
            .pointerInput(key1 = tab.id, key2 = node.id) {
                detectTapGestures(
                    onTap = {
                        if (!isDragging) {
                            onSelect()
                        }
                    }
                )
            }
            .pointerInput(key1 = tab.id, key2 = node.id) {
                detectDragGestures(
                    onDragStart = { offset ->
                        startDrag(tabPosition + offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        updateDragPosition(tabPosition + change.position)
                    },
                    onDragEnd = endDrag,
                    onDragCancel = endDrag,
                )
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tab.title,
            color = Color.Black,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Box(
            modifier = Modifier
                .size(size = 16.dp)
                .clickable(
                    onClick = onClose,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(size = 16.dp),
                tint = Color.Black,
            )
        }
    }
}

@Composable
internal fun BoxScope.DropZoneOverlay(zone: DropZone) {
    val overlayModifier = when (zone) {
        DropZone.Top -> Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .align(Alignment.TopCenter)
        DropZone.Bottom -> Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .align(Alignment.BottomCenter)
        DropZone.Left -> Modifier
            .fillMaxWidth(0.5f)
            .fillMaxHeight()
            .align(Alignment.CenterStart)
        DropZone.Right -> Modifier
            .fillMaxWidth(0.5f)
            .fillMaxHeight()
            .align(Alignment.CenterEnd)
        DropZone.Center -> Modifier.fillMaxSize()
    }

    Box(
        modifier = overlayModifier
            .background(color = Color.Blue.copy(alpha = 0.4f))
            .border(width = 2.dp, color = Color.Blue.copy(alpha = 0.8f))
    )
}

internal data class TabImpl(
    override val id: TabId = TabId(id = nextId.fetchAndIncrement().toString()),
    override val title: String = "Tab ${id.id}",
) : Tab<TabImpl> {
    override val companion = Companion

    override fun serialize(): String = id.id

    @Composable
    override fun Content() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colors[id.id.toInt() % colors.size]),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = if (id.id.toInt() % colors.size > 3) {
                    Color.White
                } else {
                    Color.Black
                },
            )
        }
    }

    companion object : TabCompanion<TabImpl> {
        private val nextId = AtomicInt(value = 0)
        private val colors = listOf(
            Color.Cyan,
            Color.Yellow,
            Color.Magenta,
            Color.LightGray,
            Color.Red,
            Color.Blue,
            Color.Green,
        )

        override fun deserialize(string: String): DeserializeResult<TabImpl> =
            try {
                string.toInt()
                DeserializeResult.Ok(
                    tab = TabImpl(
                        id = TabId(id = string),
                        title = "Tab $string",
                    )
                )
            } catch (ex: NumberFormatException) {
                DeserializeResult.Error(ex = ex)
            }
    }
}
