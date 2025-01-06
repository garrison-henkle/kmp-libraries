package dev.henkle.compose.paging

data class TransformedData<T>(
    val items: List<T>,
    val totalSizeChange: Int = 0,
    val pageSizeChanges: List<Int> = emptyList(),
)
