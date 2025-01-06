package dev.henkle.compose.sheet

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T: Any> BetterBottomSheetLayout(
    modifier: Modifier = Modifier,
    state: BetterBottomSheetLayoutState<T>,
    sheetMaxWidth: Dp = Dp.Infinity,
    sheetShape: Shape = RectangleShape,
    sheetBackgroundColor: Color = Color.Transparent,
    sheetElevation: Dp = 0.dp,
    sheetBorder: BorderStroke? = null,
    sheetVerticalArrangement: Arrangement.Vertical = Arrangement.Top,
    sheetHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrimColor: Color = BetterBottomSheetDefaults.scrimColor,
    scrimAnimationSpec: FiniteAnimationSpec<Float> = tween(),
    dismissOnOutsideTouch: Boolean = true,
    renderContentOffscreen: Boolean = false,
    sheetContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val isCollapsing: Boolean by remember(state) {
        derivedStateOf { state.sheetState.dragState.targetValue == state.collapsedState }
    }
    val isNotCollapsing: Boolean by remember { derivedStateOf { !isCollapsing } }
    val isNotCollapsed: Boolean by remember(state) {
        derivedStateOf { state.sheetState.dragState.currentValue != state.collapsedState }
    }
    val isNotAnimating: Boolean by remember { derivedStateOf { !state.sheetState.dragState.isAnimationRunning } }

    val collapsedStateOffsetYPx by remember(state) {
        derivedStateOf {
            state.sheetState.stops[state.collapsedState]?.let { collapsedAnchor ->
                state.sheetState.calculateAnchorPosition(
                    anchor = collapsedAnchor,
                    heights = state.sheetState.heightPxsState,
                )
            } ?: 0f
        }
    }
    val density = LocalDensity.current
    val fuzzyCollapsedThresholdPx by remember(density) { mutableStateOf(with(density) { 5.dp.toPx() }) }

    // This checks if the sheet is within 5dp of being collapsed as a hack for now. There is an edge case that is not
    // being covered right now, and the scrim can get stuck on the screen with a collapsed bottom sheet. This hopefully
    // prevents that case until the issue can be consistently reproduced and properly fixed
    val isFuzzyCollapsed by remember {
        derivedStateOf {
            abs(collapsedStateOffsetYPx - state.sheetState.dragState.offset) < fuzzyCollapsedThresholdPx
        }
    }

    // the sheet wants to collapse, but it is not collapsed and is not animating (because the user is
    // holding the sheet open)
    val isGesturePausedCollapse: Boolean by remember {
        derivedStateOf { isCollapsing && isNotCollapsed && isNotAnimating }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
        Crossfade(
            targetState = !isFuzzyCollapsed && (isNotCollapsing || isGesturePausedCollapse),
            animationSpec = scrimAnimationSpec,
        ) { visible ->
            if (visible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = scrimColor)
                        .consumedClickable(enabled = dismissOnOutsideTouch) {
                            scope.launch {
                                state.collapse()
                            }
                        },
                )
            }
        }
        if (renderContentOffscreen || state.isSheetVisible) {
            BetterBottomSheet(
                state = state.sheetState,
                enabled = isNotCollapsed,
                maxWidth = sheetMaxWidth,
                shape = sheetShape,
                backgroundColor = sheetBackgroundColor,
                elevation = sheetElevation,
                border = sheetBorder,
                verticalArrangement = sheetVerticalArrangement,
                horizontalAlignment = sheetHorizontalAlignment,
                content = sheetContent,
            )
        }
    }
}
