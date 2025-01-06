package dev.henkle.compose.paging

sealed interface PagerAction {
    data class LoadPage(val page: Int, val location: PageLocation) : PagerAction

    data class Refresh(val startPage: Int, val pagePreloadCount: Int) : PagerAction
}
