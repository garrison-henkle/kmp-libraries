package dev.henkle.compose.sheet

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun <T: Any> rememberBetterBottomSheetLayoutState(
    initialState: T,
    collapsedState: T,
    stops: Map<T, Anchor>,
    snapAnimationSpec: AnimationSpec<Float> = tween(),
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    confirmValueChange: (newState: T) -> Boolean = { true },
    positionalThreshold: ((totalDistance: Float) -> Float)? = null,
    velocityThreshold: (() -> Float)? = null,
    onStateReached: ((newState: T) -> Unit)? = null,
) : BetterBottomSheetLayoutState<T> {
    val sheetVisibilityState = remember { mutableStateOf(initialState != collapsedState) }
    val onStateReachedInternal: (T) -> Unit = remember(onStateReached, collapsedState) {
        { newState: T ->
            if (sheetVisibilityState.value && newState == collapsedState) {
                sheetVisibilityState.value = false
            }
            onStateReached?.invoke(newState)
        }
    }
    val sheetState = rememberBetterBottomSheetState(
        initialState = initialState,
        stops = stops,
        snapAnimationSpec = snapAnimationSpec,
        decayAnimationSpec = decayAnimationSpec,
        confirmValueChange = confirmValueChange,
        positionalThreshold = positionalThreshold,
        velocityThreshold = velocityThreshold,
        onStateReached = onStateReachedInternal,
    )
    return remember(sheetState, sheetVisibilityState, collapsedState) {
        BetterBottomSheetLayoutState(
            sheetState = sheetState,
            sheetVisibilityState = sheetVisibilityState,
            collapsedState = collapsedState,
        )
    }
}

class BetterBottomSheetLayoutState<T: Any>(
    val sheetState: BetterBottomSheetState<T>,
    sheetVisibilityState: MutableState<Boolean>,
    internal val collapsedState: T,
) {
    var isSheetVisible: Boolean by sheetVisibilityState
        internal set

    suspend fun animateToState(state: T) {
        if (state != collapsedState) {
            isSheetVisible = true
        }
        sheetState.animateToState(state = state)
    }

    suspend fun collapse() {
        sheetState.animateToState(state = collapsedState)
        isSheetVisible = false
    }
}
