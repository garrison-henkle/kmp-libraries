package dev.henkle.compose.ktab.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.henkle.compose.ktab.model.LeafNode

@Composable
internal fun TabView(
    tab: Tab,
    node: LeafNode<Tab>,
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