package dev.henkle.compose.sheet.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.henkle.compose.sheet.Anchor
import dev.henkle.compose.sheet.BetterBottomSheetLayout
import dev.henkle.compose.sheet.rememberBetterBottomSheetLayoutState
import kotlinx.coroutines.launch

enum class SheetPosition {
    Expanded,
    HalfExpanded,
    Peek,
    Collapsed,
}

@Composable
internal fun App() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(insets = WindowInsets.statusBars)
                .windowInsetsTopHeight(insets = WindowInsets.displayCutout),
        )
        BetterBottomSheetImpl(modifier = Modifier.weight(weight = 1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(insets = WindowInsets.safeDrawing),
        )
    }
}

@Composable
private fun BetterBottomSheetImpl(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val peekPx = remember(density) { with(density) { 100.dp.toPx() } }

    val state = rememberBetterBottomSheetLayoutState(
        initialState = SheetPosition.Peek,
        collapsedState = SheetPosition.Collapsed,
        stops = mapOf(
            SheetPosition.Expanded to Anchor.Max,
            SheetPosition.HalfExpanded to Anchor.Percentage(percent = 0.5f),
            SheetPosition.Peek to Anchor.Fixed(px = peekPx),
            SheetPosition.Collapsed to Anchor.Min,
        ),
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        BetterBottomSheetLayout(
            modifier = modifier.imePadding(),
            state = state,
            sheetContent = { SheetContent() },
            sheetMaxWidth = maxWidth - 40.dp,
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            renderContentOffscreen = true,
            content = { ScreenContent { state.animateToState(state = SheetPosition.Expanded) } },
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ModalBottomSheetImpl(modifier: Modifier = Modifier) {
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true,
    )

    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = bottomSheetState,
        sheetContent = { SheetContent() },
        content = { ScreenContent { bottomSheetState.show() } },
    )
}

@Composable
private fun ScreenContent(openSheet: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        reverseLayout = true
    ) {
        item {
            Spacer(modifier = Modifier.height(height = 49.dp))
            Divider(color = Color.Red)
            Spacer(modifier = Modifier.height(height = 49.dp))
            Divider(color = Color.Black)
        }
        item {
            Spacer(modifier = Modifier.height(height = 99.dp))
            Divider(color = Color.Red)
            Spacer(modifier = Modifier.height(height = 99.dp))
            Divider(color = Color.Black)
        }
        item {
            Spacer(modifier = Modifier.height(height = 149.dp))
            Divider(color = Color.Red)
            Spacer(modifier = Modifier.height(height = 149.dp))
            Divider(color = Color.Black)
        }
        item {
            Spacer(modifier = Modifier.height(height = 50.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Black lines represent the sheet stop heights. Red lines " +
                            "represent the point between two stops at which dragging " +
                            "above will snap to the top stop and dragging below will " +
                            "snap to the bottom stop (assuming default config).",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(height = 5.dp))
                Button(
                    modifier = Modifier.height(height = 16.dp),
                    onClick = {
                        scope.launch {
                            openSheet()
                        }
                    }
                ) {
                    Text(text = "open sheet", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SheetContent() {
    var text by remember { mutableStateOf("") }
    val state = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(state = state)) {
        Box(modifier = Modifier.fillMaxWidth().height(height = 100.dp).background(color = Color.Blue))
        Box(modifier = Modifier.fillMaxWidth().height(height = 100.dp).background(color = Color.Green))
        Box(modifier = Modifier.fillMaxWidth().height(height = 100.dp).background(color = Color.Red))
        Box(modifier = Modifier.fillMaxWidth().height(height = 100.dp).background(color = Color.Cyan))
        Box(modifier = Modifier.fillMaxWidth().height(height = 100.dp).background(color = Color.Yellow))
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 100.dp)
                .background(color = Color.Magenta),
            value = text,
            onValueChange = { text = it },
        )
    }
}
