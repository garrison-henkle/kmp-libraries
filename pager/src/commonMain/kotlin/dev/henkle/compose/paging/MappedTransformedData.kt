package dev.henkle.compose.paging

data class MappedTransformedData<O, T>(
    val items: List<TransformedItem<O, T>> = emptyList(),
    val totalSizeChange: Int = 0,
    val pageSizeChanges: List<Int> = emptyList(),
) {
    fun withoutMapping(): TransformedData<T> = TransformedData(
        items = items.map { it.item },
        totalSizeChange = totalSizeChange,
        pageSizeChanges = pageSizeChanges,
    )
}
