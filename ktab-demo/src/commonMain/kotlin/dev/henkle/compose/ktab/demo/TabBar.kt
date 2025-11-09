package dev.henkle.compose.ktab.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.henkle.compose.ktab.TabManager
import dev.henkle.compose.ktab.model.LeafNode

@Composable
internal fun TabBar(
    node: LeafNode<Tab>,
    manager: TabManager<Tab>,
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
            .height(height = 28.dp)
            .background(color = Color(color = 0xffeaeaea)),
    ) {
        node.tabs.forEachIndexed { index, tab ->
            TabView(
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
                onSelect = {
                    node.selectTab(index = index)
                    manager.notifyTabFocused(tab = tab)
                },
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
                        tab = Tab(),
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
