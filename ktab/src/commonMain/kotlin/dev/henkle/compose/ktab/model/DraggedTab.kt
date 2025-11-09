package dev.henkle.compose.ktab.model

import androidx.compose.runtime.Stable

internal data class DraggedTab<T: Tab<T>>(val sourceNode: LeafNode<T>, val tab: T)
