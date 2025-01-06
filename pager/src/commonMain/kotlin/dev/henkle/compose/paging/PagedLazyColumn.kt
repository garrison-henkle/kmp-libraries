package dev.henkle.compose.paging

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Suppress("ktlint:standard:function-naming")
@Composable
fun <DataType, DisplayType> PagedLazyColumn(
    modifier: Modifier,
    pageSize: Int = 25,
    maxPages: Int = 9,
    initialPagePreloadCount: Int = 2,
    startPage: Int = 0,
    loadThreshold: Int? = null,
    loadThresholdPercent: Float = 0.33f,
    loadingCirclesEnabled: Boolean = true,
    loadingCircleColor: Color = Color.Blue,
    loadingCircleSize: Dp = 48.dp,
    loadingCircleStrokeWidth: Dp = 3.dp,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    controller: PagedLazyColumnController<DisplayType>? = null,
    key: (DisplayType) -> Any,
    transform: (pages: IntRange, items: List<DataType>) -> MappedTransformedData<DataType, DisplayType>,
    reverseTransform: ((DisplayType) -> DataType?)? = null,
    fetch: suspend (offset: Int, pageSize: Int) -> List<DataType>,
    content: @Composable LazyItemScope.(item: DisplayType) -> Unit,
) {
    val pager =
        rememberPager(
            pageSize = pageSize,
            initialPagePreloadCount = initialPagePreloadCount,
            maxPages = maxPages,
            startPage = startPage,
            transform = transform,
            reverseTransform = reverseTransform,
            fetch = fetch,
        )
    val pagerData by pager.data.collectAsState()
    val pagerState by pager.state.collectAsState()
    val state = rememberLazyListState()

    DisposableEffect(controller) {
        controller?.bind(pager)
        onDispose {
            controller?.unbind()
        }
    }

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        item(key = PAGER_TOP_LOADING_CIRCLE_KEY) {
            if (loadingCirclesEnabled && pagerState.shouldShowTopLoadingCircle) {
                LoadingCircle(
                    color = loadingCircleColor,
                    size = loadingCircleSize,
                    strokeWidth = loadingCircleStrokeWidth,
                )
            }
        }

        items(items = pagerData.items, key = key, itemContent = content)

        item(key = PAGER_BOT_LOADING_CIRCLE_KEY) {
            if (loadingCirclesEnabled && pagerState.shouldShowBottomLoadingCircle) {
                LoadingCircle(
                    color = loadingCircleColor,
                    size = loadingCircleSize,
                    strokeWidth = loadingCircleStrokeWidth,
                )
            }
        }
    }

    // hacky workaround for a LazyColumn bug/weird behavior. When a new index 0 is inserted when the
    // LazyColumn is scrolled all the way up, it will jump up to the new index 0. This keeps the
    // current visible item in place.
    val density = LocalDensity.current
    LaunchedEffect(pagerData) {
        var lastFirstItem = pagerData.items.firstOrNull()
        snapshotFlow { pagerData }
            .distinctUntilChanged()
            .map { (it.pageSizeChanges.firstOrNull() ?: 0) to it.items.firstOrNull() }
            .collect { (newPageTransformAddedItemAdjustment, newFirstItem) ->
                if (lastFirstItem != newFirstItem) {
                    if (!state.canScrollBackward && lastFirstItem != null) {
                        state.scrollToItem(
                            index = pageSize + 1 + newPageTransformAddedItemAdjustment,
                            scrollOffset = if (loadingCirclesEnabled) -loadingCircleSize.px(density) else 0,
                        )
                    }
                    lastFirstItem = newFirstItem
                }
            }
    }

    registerForPagingEvents(
        pager = pager,
        state = state,
        loadThreshold = loadThreshold,
        loadThresholdPercent = loadThresholdPercent,
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun <DataType, DisplayType, ID : Any> PagedLazyColumn(
    modifier: Modifier,
    pageSize: Int = 25,
    maxPages: Int = 9,
    initialPagePreloadCount: Int = 2,
    thoroughSafetyCheck: Boolean = false,
    loadThreshold: Int? = null,
    loadThresholdPercent: Float = 0.33f,
    loadingCirclesEnabled: Boolean = true,
    loadingCircleColor: Color = Color.Blue,
    loadingCircleSize: Dp = 48.dp,
    loadingCircleStrokeWidth: Dp = 3.dp,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    controller: PagedLazyColumnController<DisplayType>? = null,
    key: (DisplayType) -> Any,
    getID: (DataType) -> ID,
    transform: (pages: IntRange, items: List<DataType>) -> MappedTransformedData<DataType, DisplayType>,
    reverseTransform: ((DisplayType) -> DataType?)? = null,
    fetch: suspend (lastID: ID?, pageSize: Int) -> List<DataType>,
    content: @Composable LazyItemScope.(item: DisplayType) -> Unit,
) {
    val pager =
        rememberPager(
            pageSize = pageSize,
            initialPagePreloadCount = initialPagePreloadCount,
            maxPages = maxPages,
            thoroughSafetyCheck = thoroughSafetyCheck,
            getID = getID,
            transform = transform,
            reverseTransform = reverseTransform,
            fetch = fetch,
        )
    val pagerData by pager.data.collectAsState()
    val pagerState by pager.state.collectAsState()
    val state = rememberLazyListState()

    DisposableEffect(controller) {
        controller?.bind(pager)
        onDispose {
            controller?.unbind()
        }
    }

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        item(key = PAGER_TOP_LOADING_CIRCLE_KEY) {
            if (loadingCirclesEnabled && pagerState.shouldShowTopLoadingCircle) {
                LoadingCircle(
                    color = loadingCircleColor,
                    size = loadingCircleSize,
                    strokeWidth = loadingCircleStrokeWidth,
                )
            }
        }

        items(items = pagerData.items, key = key, itemContent = content)

        item(key = PAGER_BOT_LOADING_CIRCLE_KEY) {
            if (loadingCirclesEnabled && pagerState.shouldShowBottomLoadingCircle) {
                LoadingCircle(
                    color = loadingCircleColor,
                    size = loadingCircleSize,
                    strokeWidth = loadingCircleStrokeWidth,
                )
            }
        }
    }

    // hacky workaround for a LazyColumn bug/weird behavior. When a new index 0 is inserted when the
    // LazyColumn is scrolled all the way up, it will jump up to the new index 0. This keeps the
    // current visible item in place.
    val density = LocalDensity.current
    LaunchedEffect(pagerData) {
        var lastFirstItem = pagerData.items.firstOrNull()
        snapshotFlow { pagerData }
            .distinctUntilChanged()
            .map { (it.pageSizeChanges.firstOrNull() ?: 0) to it.items.firstOrNull() }
            .collect { (newPageTransformAddedItemAdjustment, newFirstItem) ->
                if (lastFirstItem != newFirstItem) {
                    if (!state.canScrollBackward && lastFirstItem != null) {
                        state.scrollToItem(
                            index = pageSize + 1 + newPageTransformAddedItemAdjustment,
                            scrollOffset = if (loadingCirclesEnabled) -loadingCircleSize.px(density) else 0,
                        )
                    }
                    lastFirstItem = newFirstItem
                }
            }
    }

    registerForPagingEvents(
        pager = pager,
        state = state,
        loadThreshold = loadThreshold,
        loadThresholdPercent = loadThresholdPercent,
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun LoadingCircle(
    color: Color,
    size: Dp,
    strokeWidth: Dp,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round,
        )
    }
}

private const val PAGER_TOP_LOADING_CIRCLE_KEY = "pagerTopLoadingCircleKey"
private const val PAGER_BOT_LOADING_CIRCLE_KEY = "pagerBotLoadingCircleKey"

private fun Dp.px(density: Density): Int = with(density) { roundToPx() }

private val PagerState.shouldShowTopLoadingCircle: Boolean get() =
    this is PagerState.LoadingAtStart

private val PagerState.shouldShowBottomLoadingCircle: Boolean get() =
    this is PagerState.LoadingAtEnd || this is PagerState.Refresh
