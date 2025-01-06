package dev.henkle.compose.sheet

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

internal fun Modifier.consumedClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier = then(
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            waitForUpOrCancellation()?.also {
                if (enabled) {
                    onClick()
                }
            }
        }
    }
)
