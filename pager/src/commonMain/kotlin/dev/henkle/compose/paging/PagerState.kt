package dev.henkle.compose.paging

sealed class PagerState {
    data object Refresh : PagerState()

    data object LoadingAtStart : PagerState()

    data object LoadingAtEnd : PagerState()

    data object Idle : PagerState()

    val isLoading: Boolean get() = this !is Idle
}
