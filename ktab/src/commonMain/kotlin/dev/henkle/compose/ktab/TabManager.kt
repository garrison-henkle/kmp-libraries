package dev.henkle.compose.ktab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import dev.henkle.compose.ktab.model.DraggedTab
import dev.henkle.compose.ktab.model.DropZone
import dev.henkle.compose.ktab.model.HoveredNode
import dev.henkle.compose.ktab.model.LeafNode
import dev.henkle.compose.ktab.model.NodeId
import dev.henkle.compose.ktab.model.PaneNode
import dev.henkle.compose.ktab.model.SplitNode
import dev.henkle.compose.ktab.model.SplitOrientation
import dev.henkle.compose.ktab.model.Tab
import dev.henkle.compose.ktab.model.TabId
import dev.henkle.compose.ktab.ui.TabLayout
import dev.henkle.datastructures.queue.MutableQueue

/**
 * The manager of a drag-and-drop tab system.
 *
 * A [TabManager] maintains the tab system via a node tree
 * where each node is either a leaf node that contains tabs
 * or a split node that contains two child nodes and a split
 * ratio for determining how they are rendered.
 *
 * The [addTab] overloads can be used to add nodes to either
 * a specified node or the first leaf node found.
 *
 * The [closeTab] overloads can be used to remove tabs from the
 * tree. Both may cause the leaf containing the tab to itself
 * be removed from the tree if it becomes empty as a result.
 *
 * [findFirstLeaf] and [findParentLeaf] can be used to find leaf
 * nodes within the node tree.
 *
 * @property dropZoneThreshold the ratio of the tab's width and
 * height that will be considered part of its drop zones. For
 * example, a value of 0.25 means that the left 25% of a tab
 * will be its left split drop zone, the top 25% will be its
 * top split drop zone, etc. Any remaining space will be used
 * for the center drop zone (used to add tabs to a node without
 * splitting it)
 * @property defaultSplitRatio the default ratio used to split
 * the available screen space of a node between its children.
 * For example, a 0.5 ratio will evenly split the available
 * screen space between the two children.
 */
class TabManager<T: Tab<T>> internal constructor(
    private val dropZoneThreshold: Float,
    private val defaultSplitRatio: Float,
) {
    // tab/node state

    internal var root by mutableStateOf<PaneNode<T>>(value = LeafNode(parent = null))
        private set

    internal var draggedTab by mutableStateOf<DraggedTab<T>?>(value = null)
        private set
    internal var dragPosition by mutableStateOf<Offset?>(value = null)
        private set

    internal var hoveredNode by mutableStateOf<HoveredNode<T>?>(value = null)
        private set

    /**
     * The last tab that had some sort of user input (e.g. recently dragged, split, or clicked)
     */
    var lastFocusedTab by mutableStateOf<T?>(value = null)
        private set

    /**
     * Indicates whether a drag is currently being tracked by this [TabManager]
     */
    val isDragging: Boolean
        get() = draggedTab != null

    private val nodeBounds = mutableMapOf<NodeId, Pair<LeafNode<T>, Rect>>()

    // tab management methods

    /**
     * Adds a tab to the specified node
     *
     * @param node the node the tab will be added to
     * @param tab the tab to add
     */
    fun addTab(node: LeafNode<T>, tab: T) {
        insertTab(
            targetNode = node,
            tab = tab,
            zone = DropZone.Center,
        )
    }

    /**
     * Adds a tab to the first leaf node found in the node tree
     *
     * @param tab the tab to add
     *
     * @return true if a leaf node was found and the tab was
     * added, false otherwise
     */
    fun addTab(tab: T): Boolean =
        findFirstLeaf()?.also { leafNode ->
            insertTab(
                targetNode = leafNode,
                tab = tab,
                zone = DropZone.Center,
            )
        } != null

    /**
     * Closes a tab within a leaf node.
     *
     * If the leaf node is empty after the tab is closed, it
     * will be deleted as long as it is not the root node of
     * the node tree.
     *
     * @param node the node the tab will be removed from. This
     * node may be removed from the node tree
     * @param tabId the id of the tab to remove
     *
     * @return true if the tab was removed, false otherwise
     */
    fun closeTab(node: LeafNode<T>, tabId: TabId): Boolean =
        removeTabAndDeleteNodeIfNecessary(node = node, tabId = tabId)

    /**
     * Finds the parent leaf node of the provided tab and removes
     * the tab from it.
     *
     * If the parent leaf node is empty after the tab is closed,
     * it will be deleted as long as it is not the root node of
     * the node tree.
     *
     * If the leaf node containing tab is already known, consider
     * using the other overload to avoid the need to iterate
     * over the node tree.
     *
     * @param tabId the id of the tab to remove from the tree
     *
     * @return true if the tab was removed, false otherwise
     */
    fun closeTab(tabId: TabId): Boolean =
        findParentLeaf(tabId = tabId)
            ?.let { parentLeaf ->
                removeTabAndDeleteNodeIfNecessary(node = parentLeaf, tabId = tabId)
            } ?: false

    /**
     * Finds the first leaf node within the node tree using breadth-first search.
     *
     * @return the first leaf node or null if none were found
     */
    fun findFirstLeaf(): LeafNode<T>? {
        val queue = MutableQueue<PaneNode<T>>()
            .apply { add(element = root) }

        var current: PaneNode<T>
        while (queue.isNotEmpty()) {
            current = queue.poll()!!
            when (current) {
                is LeafNode -> return current
                is SplitNode -> {
                   queue.add(element = current.first)
                   queue.add(element = current.second)
                }
            }
        }

        return null
    }

    /**
     * Finds the leaf node that contains the tab identified by [tabId]
     *
     * @param tabId the id of the tab to find
     *
     * @return the leaf node that contains the tab or null if
     * the tab could not be found
     */
    fun findParentLeaf(tabId: TabId): LeafNode<T>? {
        val queue = MutableQueue<PaneNode<T>>()
            .apply { add(element = root) }

        var current: PaneNode<T>
        while (queue.isNotEmpty()) {
            current = queue.poll()!!
            when (current) {
                is LeafNode -> {
                    if (current.tabs.any { it.id == tabId }) {
                        return current
                    }
                }
                is SplitNode -> {
                    queue.add(element = current.first)
                    queue.add(element = current.second)
                }
            }
        }

        return null
    }

    // drag methods

    /**
     * Starts the drag-and-drop flow for a tab
     *
     * @param sourceNode the node that contains the tab
     * @param tab the tab that is being dragged
     * @param initialOffset the initial offset of the dragging
     * pointer within the [TabLayout]
     */
    fun startDrag(
        sourceNode: LeafNode<T>,
        tab: T,
        initialOffset: Offset
    ) {
        this.draggedTab = DraggedTab(
            sourceNode = sourceNode,
            tab = tab,
        )
        dragPosition = initialOffset
    }

    /**
     * Notifies the [TabManager] of the drag pointers
     * current position.
     *
     * This updates the hovered tab when dragging.
     */
    fun updateDragPosition(position: Offset) {
        dragPosition = position
    }

    /**
     * Notifies the [TabManager] that a drag-and-drop
     * flow has been completed.
     *
     * If the pointer was above a valid node, the dragged
     * tab will either split or be inserted into the hovered
     * node.
     */
    fun endDrag() {
        draggedTab?.also { draggedTab ->
            hoveredNode?.also { hoveredNode ->
                dropTab(
                    targetNode = hoveredNode.node,
                    tab = draggedTab.tab,
                    zone = hoveredNode.zone,
                    sourceNode = draggedTab.sourceNode,
                )
            }
        }

        draggedTab = null
        dragPosition = null
    }

    internal fun dropTab(
        sourceNode: LeafNode<T>,
        targetNode: LeafNode<T>,
        tab: T,
        zone: DropZone,
    ) {
        if (targetNode.id == sourceNode.id && zone == DropZone.Center) {
            if (targetNode.selectedTab.id != tab.id) {
                val tabIndex = targetNode.tabs.indexOf(element = tab)
                if (tabIndex == -1) {
                    error(
                        message = "It shouldn't be possible for a tab " +
                            "to have the nodeId of a node but not be in " +
                            "that node's tab list!",
                    )
                }
                targetNode.selectedIndex = tabIndex
                notifyTabFocused(tab = targetNode.tabs[tabIndex])
            }
            return
        }
        removeTabAndDeleteNodeIfNecessary(node = sourceNode, tabId = tab.id)
        insertTab(
            targetNode = targetNode,
            tab = tab,
            zone = zone,
        )
    }

    // focus methods

    /**
     * Sets the provided tab as the last focused tab in this tab manager
     */
    fun notifyTabFocused(tab: T) {
        lastFocusedTab = tab
    }

    // position and drop zone methods

    internal suspend fun observeDrags() {
        snapshotFlow { dragPosition }
            .collect { offset ->
                hoveredNode = offset
                    ?.let(::determineHoveredTab)
                    ?.let { (hoveredNode, bounds) ->
                        val zone = determineDropZone(
                            offset = offset,
                            bounds = bounds,
                        )
                        HoveredNode(
                            node = hoveredNode,
                            zone = zone,
                        )
                    }
            }
    }

    internal fun updateNodePosition(
        node: LeafNode<T>,
        bounds: Rect,
    ) {
        nodeBounds[node.id] = node to bounds
    }

    private fun determineHoveredTab(offset: Offset): Pair<LeafNode<T>, Rect>? {
        for (nodeData in nodeBounds.values) {
            if (offset in nodeData.second) {
                return nodeData
            }
        }
        return null
    }

    private fun determineDropZone(
        offset: Offset,
        bounds: Rect,
    ): DropZone {
        if (bounds.width == 0f || bounds.height == 0f) {
            return DropZone.Center
        }

        val (distance, closestZone) = listOf(
            (offset.x - bounds.left).coerceAtLeast(minimumValue = 0f) to DropZone.Left,
            (bounds.right - offset.x).coerceAtLeast(minimumValue = 0f) to DropZone.Right,
            (offset.y - bounds.top).coerceAtLeast(minimumValue = 0f) to DropZone.Top,
            (bounds.bottom - offset.y).coerceAtLeast(minimumValue = 0f) to DropZone.Bottom,
        ).minBy { (distance) -> distance }

        val distanceAsRatio = if (closestZone.isHorizontal) {
            distance / bounds.width
        } else {
            distance / bounds.height
        }

        return if (distanceAsRatio <= dropZoneThreshold) {
            closestZone
        } else {
            DropZone.Center
        }
    }

    // internal helpers

    private fun removeTabAndDeleteNodeIfNecessary(
        node: LeafNode<T>,
        tabId: TabId,
    ): Boolean {
        val (removed, shouldDeleteNode) = removeTab(
            node = node,
            tabId = tabId,
        )
        if (shouldDeleteNode) {
            nodeBounds.remove(key = node.id)

            node.parent?.also { parent ->
                val survivingNode = if (parent.first.id == node.id) {
                    parent.second
                } else {
                    parent.first
                }
                survivingNode.parent = parent.parent
                parent.parent?.also { grandparent ->
                    if (grandparent.first.id == parent.id) {
                        grandparent.first = survivingNode
                    } else {
                        grandparent.second = survivingNode
                    }
                } ?: run {
                    root = survivingNode
                }
            } ?: run {
                root = LeafNode(parent = null)
            }
        }

        return removed
    }

    data class TabRemovalResult(val removed: Boolean, val leafIsEmpty: Boolean)

    private fun removeTab(
        node: LeafNode<T>,
        tabId: TabId,
    ): TabRemovalResult {
        var removed = false
        var leafIsEmpty = false
        val newTabs = node.tabs.toMutableList()
        for (i in newTabs.indices) {
            if (newTabs[i].id == tabId) {
                newTabs.removeAt(index = i)
                removed = true
                if (newTabs.isEmpty()) {
                    leafIsEmpty = true
                } else {
                    node.tabs = newTabs
                    if (node.selectedIndex >= newTabs.size) {
                        node.selectedIndex = newTabs.lastIndex
                    }
                }
                break
            }
        }
        return TabRemovalResult(removed = removed, leafIsEmpty = leafIsEmpty)
    }

    private fun insertTab(
        targetNode: LeafNode<T>,
        tab: T,
        zone: DropZone,
    ) {
        val targetNodeParent = targetNode.parent
        val targetNodeReplacement = when (zone) {
            DropZone.Center -> {
                val newTabs = targetNode.tabs + tab
                targetNode.tabs = newTabs
                targetNode.selectTab(index = newTabs.lastIndex)
                null
            }
            DropZone.Top -> {
                SplitNode(
                    orientation = SplitOrientation.Vertical,
                    parent = targetNode.parent,
                    first = targetNode,
                    second = targetNode,
                    splitRatio = defaultSplitRatio,
                ).apply {
                    first = LeafNode(parent = this, tabs = listOf(tab))
                    second.parent = this
                }
            }
            DropZone.Bottom -> {
                SplitNode(
                    orientation = SplitOrientation.Vertical,
                    parent = targetNode.parent,
                    first = targetNode,
                    second = targetNode,
                    splitRatio = defaultSplitRatio,
                ).apply {
                    first.parent = this
                    second = LeafNode(parent = this, tabs = listOf(tab))
                }
            }
            DropZone.Left -> {
                SplitNode(
                    orientation = SplitOrientation.Horizontal,
                    parent = targetNode.parent,
                    first = targetNode,
                    second = targetNode,
                    splitRatio = defaultSplitRatio,
                ).apply {
                    first = LeafNode(parent = this, tabs = listOf(tab))
                    second.parent = this
                }
            }
            DropZone.Right -> {
                SplitNode(
                    orientation = SplitOrientation.Horizontal,
                    parent = targetNode.parent,
                    first = targetNode,
                    second = targetNode,
                    splitRatio = defaultSplitRatio,
                ).apply {
                    first.parent = this
                    second = LeafNode(parent = this, tabs = listOf(tab))
                }
            }
        }

        notifyTabFocused(tab = tab)

        if (targetNodeReplacement != null) {
            targetNodeParent?.also { parent ->
                if (parent.first.id == targetNode.id) {
                    parent.first = targetNodeReplacement
                } else {
                    parent.second = targetNodeReplacement
                }
            } ?: run {
                root = targetNodeReplacement
            }
        }
    }

    companion object {
        internal const val DEFAULT_DROP_ZONE_THRESHOLD = 0.33f
        internal const val DEFAULT_SPLIT_RATIO = 0.5f
    }
}
