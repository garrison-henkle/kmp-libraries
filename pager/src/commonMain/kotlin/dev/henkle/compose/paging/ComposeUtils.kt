package dev.henkle.compose.paging

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

@Composable
fun <T> rememberPagedLazyColumnController(): PagedLazyColumnController<T> = remember { PagedLazyColumnController() }

@Composable
fun <O, T, ID : Any> rememberPager(
    pageSize: Int = 50,
    maxPages: Int = 9,
    initialPagePreloadCount: Int = 2,
    thoroughSafetyCheck: Boolean = false,
    getID: (item: O) -> ID,
    transform: (pages: IntRange, items: List<O>) -> MappedTransformedData<O, T>,
    reverseTransform: ((item: T) -> O?)? = null,
    fetch: suspend (lastID: ID?, pageSize: Int) -> List<O>,
): PagerAdapter<T> {
    val scope = rememberCoroutineScope { Dispatchers.Main }
    val pager =
        remember {
            IDPagerAdapter(
                pageSize = pageSize,
                maxPages = maxPages,
                pagePreloadCount = initialPagePreloadCount,
                scope = scope,
                enableThoroughSafetyCheck = thoroughSafetyCheck,
                getID = getID,
                transform = transform,
                reverseTransform = reverseTransform,
                fetch = fetch,
            )
        }

    DisposableEffect(Unit) {
        pager.startFlows()
        onDispose { pager.shutdown() }
    }

    return pager
}

@Composable
fun <O, T> rememberPager(
    pageSize: Int = 50,
    maxPages: Int = 9,
    initialPagePreloadCount: Int = 2,
    startPage: Int = 0,
    transform: (pages: IntRange, items: List<O>) -> MappedTransformedData<O, T>,
    reverseTransform: ((item: T) -> O?)? = null,
    fetch: suspend (offset: Int, pageSize: Int) -> List<O>,
): PagerAdapter<T> {
    val scope = rememberCoroutineScope { Dispatchers.Main }
    val pager =
        remember {
            OffsetPagerAdapter(
                pageSize = pageSize,
                maxPages = maxPages,
                pagePreloadCount = initialPagePreloadCount,
                startPage = startPage,
                scope = scope,
                transform = transform,
                reverseTransform = reverseTransform,
                fetch = fetch,
            )
        }

    DisposableEffect(Unit) {
        pager.startFlows()
        onDispose { pager.shutdown() }
    }

    return pager
}

@Composable
fun <T> registerForPagingEvents(
    pager: PagerAdapter<T>,
    state: LazyListState,
    loadThreshold: Int? = null,
    loadThresholdPercent: Float = 0.33f,
) {
    val pagerData by pager.data.collectAsState()
    LaunchedEffect(state, pager) {
        var lastFirstIndex = 0
        snapshotFlow {
            Triple(state.firstVisibleItemIndex, state.layoutInfo.visibleItemsInfo.size, pagerData)
        }.map { (first, count, data) ->
            val scrollingDown = first > lastFirstIndex
            val last = first + count
            val threshold = loadThreshold ?: (loadThresholdPercent * pager.pageSize).roundToInt()
            val approachingTop = first < threshold && data.items.size >= pager.pageSize
            val approachingBottom = scrollingDown && last > (data.items.size - threshold) && data.items.size >= pager.pageSize
            lastFirstIndex = first
            approachingTop to approachingBottom
        }.distinctUntilChanged().collect { (approachingTop, approachingBottom) ->
            if (approachingBottom) {
                pager.loadNext()
            } else if (approachingTop) {
                pager.loadPrevious()
            }
        }
    }
}

fun <T> Modifier.pagingListener(
    pager: PagerAdapter<T>,
    state: LazyListState,
    loadThreshold: Int? = null,
    loadThresholdPercent: Float = 0.33f,
) = composed {
    registerForPagingEvents(
        pager = pager,
        state = state,
        loadThreshold = loadThreshold,
        loadThresholdPercent = loadThresholdPercent,
    )
    this
}
