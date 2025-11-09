package dev.henkle.compose.ktab.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.uuid.Uuid

/**
 * A split node within the tab node system. Split nodes are responsible
 * for creating and maintaining the split-screen hierarchy that allows
 * for multiple tabs to be displayed at once. Each split node contains
 * two children, [first] and [second], who share the available screen
 * space according to the value of [splitRatio].
 *
 * @property orientation the orientation used to split the screen space
 * between the node's children e.g. [SplitOrientation.Horizontal] will
 * split the horizontal screen space between the children
 */
class SplitNode<T: Tab<T>> internal constructor(
    override val id: NodeId = NodeId(id = Uuid.random().toString()),
    parent: SplitNode<T>?,
    val orientation: SplitOrientation,
    first: PaneNode<T>,
    second: PaneNode<T>,
    splitRatio: Float = 0.5f
) : PaneNode<T>() {
    override var parent by mutableStateOf(value = parent)
        internal set

    /**
     * The first child of the split. When [orientation] is
     * [SplitOrientation.Horizontal], this child is the left
     * node. When [orientation] is [SplitOrientation.Vertical],
     * this child is the top node.
     */
    var first by mutableStateOf(value = first)
        internal set

    /**
     * The second child of the split. When [orientation] is
     * [SplitOrientation.Horizontal], this child is the right
     * node. When [orientation] is [SplitOrientation.Vertical],
     * this child is the bottom node.
     */
    var second by mutableStateOf(value = second)
        internal set

    /**
     * The ratio of the available screen space given to the
     * [first] child
     */
    var splitRatio by mutableFloatStateOf(value = splitRatio)
        internal set

    override fun toString(): String =
        listOf(
            "id=$id",
            "parent=${parent?.id}",
            "orientation=$orientation",
            "splitRatio=$splitRatio",
            "first=$first",
            "second=$second",
        ).joinToString(
            separator = ", ",
            prefix = "SplitNode(",
            postfix = ")",
        )
}
