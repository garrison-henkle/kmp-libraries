package dev.henkle.compose.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.jvm.JvmName
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T: Any> BetterBottomSheet(
    modifier: Modifier = Modifier,
    state: BetterBottomSheetState<T>,
    enabled: Boolean = true,
    maxWidth: Dp = Dp.Infinity,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Transparent,
    elevation: Dp = 0.dp,
    border: BorderStroke? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    SubcomposeLayout(
        modifier = modifier
            .offset { IntOffset(x = 0, y = state.dragState.requireOffset().roundToInt()) },
    ) { constraints ->
        val boundedMaxWidth = constraints.maxWidth
            .coerceAtMost(maximumValue = maxWidth.roundToPx())

        val placeable = subcompose(slotId = null){
            Surface(
                modifier = Modifier
                    .nestedScroll(
                        remember(state.dragState) {
                            ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
                                state = state.dragState,
                                orientation = Orientation.Vertical,
                            )
                        }
                    ).anchoredDraggable(
                        state = state.dragState,
                        orientation = Orientation.Vertical,
                        enabled = enabled,
                    ),
                shape = shape,
                color = backgroundColor,
                elevation = elevation,
                border = border,
            ) {
                Column(
                    verticalArrangement = verticalArrangement,
                    horizontalAlignment = horizontalAlignment,
                    content = content,
                )
            }
        }.first().measure(constraints = constraints.copy(minHeight = 0, maxWidth = boundedMaxWidth))

        val contentHeight = placeable.measuredHeight
            .coerceAtMost(maximumValue = placeable.height)
            .coerceAtMost(maximumValue = constraints.maxHeight)

        state.heightPxs = CalculationPxHeights(
            containerHeight = constraints.maxHeight.toFloat(),
            content = contentHeight.toFloat(),
        )

        layout(width = placeable.width, height = constraints.maxHeight) {
            placeable.place(0, 0)
        }
    }
}

// Stolen from androidx.compose.material.ModalBottomSheet.kt
@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
private fun ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
    state: AnchoredDraggableState<*>,
    orientation: Orientation
): NestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.toFloat()
        return if (delta < 0 && source == NestedScrollSource.UserInput) {
            state.dispatchRawDelta(delta).toOffset()
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return if (source == NestedScrollSource.UserInput) {
            state.dispatchRawDelta(available.toFloat()).toOffset()
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val toFling = available.toFloat()
        val currentOffset = state.requireOffset()
        return if (toFling < 0 && currentOffset > state.anchors.minAnchor()) {
            state.settle(velocity = toFling)
            // since we go to the anchor with tween settling, consume all for the best UX
            available
        } else {
            Velocity.Zero
        }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        state.settle(velocity = available.toFloat())
        return available
    }

    private fun Float.toOffset(): Offset = Offset(
        x = if (orientation == Orientation.Horizontal) this else 0f,
        y = if (orientation == Orientation.Vertical) this else 0f
    )

    @JvmName("velocityToFloat")
    private fun Velocity.toFloat() = if (orientation == Orientation.Horizontal) x else y

    @JvmName("offsetToFloat")
    private fun Offset.toFloat(): Float = if (orientation == Orientation.Horizontal) x else y
}
