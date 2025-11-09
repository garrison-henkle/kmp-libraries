package dev.henkle.compose.ktab.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.uuid.Uuid

/**
 * A leaf node within the tab node system. Leaf nodes contain the tabs
 * and are responsible for tracking which of their tabs are currently
 * selected.
 */
class LeafNode<T: Tab<T>> internal constructor(
    override val id: NodeId = NodeId(id = Uuid.random().toString()),
    parent: SplitNode<T>?,
    tabs: List<T> = emptyList(),
    selectedIndex: Int = 0
) : PaneNode<T>() {
    override var parent by mutableStateOf(value = parent)
        internal set
    // avoiding SnapshotStateList here out of PTSD

    /**
     * The current tabs contained in this leaf
     */
    var tabs by mutableStateOf(value = tabs)
        internal set

    /**
     * The index of the tab that is currently selected within this leaf
     */
    var selectedIndex by mutableIntStateOf(value = selectedIndex)
        internal set

    /**
     * The tab that is currently selected within this leaf
     */
    val selectedTab: T get() = tabs[selectedIndex]

    /**
     * Selects the tab at the provided index
     *
     * @param the index of the tab to select
     *
     * @return true if the tab was selected or false if no tab
     * exists at the provided index
     */
    fun selectTab(index: Int): Boolean =
        if (index in tabs.indices) {
            selectedIndex = index
            true
        } else {
            false
        }

    override fun toString(): String =
        listOf(
            "id=$id",
            "parent=${parent?.id}",
            "selectedIndex=$selectedIndex",
            "tabs=$tabs",
        ).joinToString(
            separator = ", ",
            prefix = "LeafNode(",
            postfix = ")",
        )
}
