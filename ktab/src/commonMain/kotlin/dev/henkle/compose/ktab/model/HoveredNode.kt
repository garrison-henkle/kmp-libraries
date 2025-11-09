package dev.henkle.compose.ktab.model

internal data class HoveredNode<T: Tab<T>>(val node: LeafNode<T>, val zone: DropZone)
