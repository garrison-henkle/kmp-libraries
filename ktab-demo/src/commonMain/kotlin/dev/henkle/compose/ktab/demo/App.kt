@file:OptIn(ExperimentalAtomicApi::class)
@file:Suppress("UNCHECKED_CAST")

package dev.henkle.compose.ktab.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.henkle.compose.ktab.TabManager
import dev.henkle.compose.ktab.model.*
import dev.henkle.compose.ktab.ui.TabLayout
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Composable
internal fun App(manager: TabManager<Tab>) {
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
