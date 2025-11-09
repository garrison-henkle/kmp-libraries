package dev.henkle.compose.ktab.model

import androidx.compose.runtime.Composable

/**
 * A tab managed by the tab system
 */
interface Tab<T: Tab<T>> {
    /**
     * The unique id of the tab
     */
    val id: TabId

    /**
     * The title of the tab that will appear in the tab bar
     */
    val title: String

    /**
     * Serializes this tab to a string
     */
    fun serialize(): String

    /**
     * The content displayed when this tab is selected
     */
    @Composable
    fun Content()

    /**
     * The companion of this tab type that contains a deserializer
     */
    val companion: TabCompanion<T>
}
