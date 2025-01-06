package dev.henkle.compose.sheet

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@Composable
fun <T: Any> rememberBetterBottomSheetState(
    initialState: T,
    stops: Map<T, Anchor>,
    snapAnimationSpec: AnimationSpec<Float> = tween(),
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    confirmValueChange: (newState: T) -> Boolean = { true },
    positionalThreshold: ((totalDistance: Float) -> Float)? = null,
    velocityThreshold: (() -> Float)? = null,
    onStateReached: ((newState: T) -> Unit)? = null,
): BetterBottomSheetState<T> {
    val density = LocalDensity.current
    val screenSizePx = getScreenSize()
    val scope = rememberCoroutineScope()
    return remember(
        density,
        screenSizePx,
        scope,
        initialState,
        stops,
        snapAnimationSpec,
        decayAnimationSpec,
        confirmValueChange,
        positionalThreshold,
        velocityThreshold,
        onStateReached,
    ) {
        BetterBottomSheetState(
            initialState = initialState,
            stops = stops,
            snapAnimationSpec = snapAnimationSpec,
            decayAnimationSpec = decayAnimationSpec,
            initialScreenHeightPx = screenSizePx.height,
            density = density,
            scope = scope,
            confirmValueChange = confirmValueChange,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
            onStateReached = onStateReached,

        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
class BetterBottomSheetState<T: Any>(
    initialState: T,
    internal val stops: Map<T, Anchor>,
    initialScreenHeightPx: Float,
    density: Density,
    private val scope: CoroutineScope,
    snapAnimationSpec: AnimationSpec<Float>,
    decayAnimationSpec: DecayAnimationSpec<Float>,
    confirmValueChange: (newState: T) -> Boolean = { true },
    positionalThreshold: ((totalDistance: Float) -> Float)? = null,
    velocityThreshold: (() -> Float)? = null,
    onStateReached: ((newState: T) -> Unit)? = null,
) {
    init {
        if(stops.count { it.value == Anchor.Max } > 1 || stops.count { it.value == Anchor.Min } > 1) {
            throw IllegalStateException("There can only be one each of Anchor.Min and Anchor.Max!")
        }
    }

    private val anchorsToKeys = mutableMapOf<Anchor, T>().apply {
        for (stop in stops) {
            put(key = stop.value, value = stop.key)
        }
    }.toMap()

    private val _heightUpdates = MutableSharedFlow<CalculationPxHeights>()
    @Suppress("unused")
    val heightUpdates: Flow<CalculationPxHeights> = _heightUpdates.asSharedFlow()
    private var initialHeightSet = true
    private val _heightPxsState = mutableStateOf(CalculationPxHeights(initialScreenHeightPx, initialScreenHeightPx))
    internal var heightPxsState by _heightPxsState
    internal var heightPxs
        set(newHeightPxs) {
            if (_heightPxsState.value != newHeightPxs) {
                val newAnchors = calculateAnchors(heights = newHeightPxs)
                if (initialHeightSet) {
                    dragState.updateAnchors(
                        newAnchors = newAnchors,
                        newTarget = dragState.targetValue,
                    )
                    initialHeightSet = false
                } else {
                    val newTarget = when(val currentAnchor = stops[dragState.targetValue]) {
                        is Anchor.Max,
                        is Anchor.Min -> anchorsToKeys[currentAnchor]
                            ?: dragState.getClosestTargetOrFallback(newAnchors = newAnchors)

                        else -> dragState.getClosestTargetOrFallback(newAnchors = newAnchors)
                    }
                    dragState.updateAnchors(
                        newAnchors = newAnchors,
                        newTarget = newTarget,
                    )
                }
                _heightPxsState.value = newHeightPxs
                scope.launch {
                    _heightUpdates.emit(newHeightPxs)
                }
            }
        }
        get() = _heightPxsState.value

    val dragState = AnchoredDraggableState(
        initialValue = initialState,
        anchors = calculateAnchors(),
        positionalThreshold = positionalThreshold ?: ::calculatePositionalThreshold,
        velocityThreshold = velocityThreshold ?: BetterBottomSheetDefaults.velocityThreshold(density),
        confirmValueChange = confirmValueChange,
        decayAnimationSpec = decayAnimationSpec,
        snapAnimationSpec = snapAnimationSpec,
    )

    @Suppress("UNUSED_PARAMETER")
    private fun calculatePositionalThreshold(totalDistance: Float): Float =
        stops[dragState.currentValue]?.offsetPx()?.let { currentStopValue ->
            val isHidingContent = (heightPxs.containerHeight - dragState.offset) < currentStopValue
            val stops = getSortedStops()
            stops.indexOfFirst { currentStopValue == it }
                .takeIf { it != -1 }
                ?.let { currentStopIndex ->
                    if (isHidingContent) {
                        currentStopIndex - 1
                    } else {
                        currentStopIndex + 1
                    }.takeIf { index -> index in stops.indices }
                        ?.let { targetStopIndex ->
                            val targetStopValue = stops[targetStopIndex]
                            val distanceBetweenStops = if (isHidingContent) {
                                targetStopValue - currentStopValue
                            } else {
                                currentStopValue - targetStopValue
                            }
                            distanceBetweenStops * BetterBottomSheetDefaults.SNAP_SCREEN_PERCENT
                        }
                }
        } ?: (heightPxs.content * BetterBottomSheetDefaults.SNAP_SCREEN_PERCENT)

    private fun getSortedStops() = stops.values.map { it.offsetPx() }.sorted()

    private fun calculateAnchors(heights: CalculationPxHeights = heightPxs): DraggableAnchors<T> =
        DraggableAnchors {
            for ((key, anchor) in stops) {
                key at calculateAnchorPosition(anchor = anchor, heights = heights)
            }
        }

    internal fun calculateAnchorPosition(anchor: Anchor, heights: CalculationPxHeights = heightPxs): Float =
        when (anchor) {
            is Anchor.Fixed -> heights.containerHeight - anchor.px
            is Anchor.Percentage -> heights.containerHeight - (heights.content * anchor.percent)
            is Anchor.Min -> heights.containerHeight
            is Anchor.Max -> heights.containerHeight - heights.content
        }

    private fun Anchor.offsetPx(): Float = when(this) {
        is Anchor.Min -> 0f
        is Anchor.Fixed -> px
        is Anchor.Percentage -> heightPxs.content * percent
        is Anchor.Max -> heightPxs.content
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun AnchoredDraggableState<T>.getClosestTargetOrFallback(newAnchors: DraggableAnchors<T>): T =
        if (!offset.isNaN()) {
            newAnchors.closestAnchor(offset) ?: targetValue
        } else targetValue

    init {
        scope.launch {
            snapshotFlow { dragState.targetValue to dragState.currentValue }
                .collect { (target, current) ->
                    if (target == current) {
                        onStateReached?.invoke(current)
                    }
                }
        }
    }

    suspend fun animateToState(state: T) {
        dragState.animateTo(targetValue = state)
    }
}

data class CalculationPxHeights(val containerHeight: Float, val content: Float)
