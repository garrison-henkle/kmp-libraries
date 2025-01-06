package dev.henkle.markdown.parser.jetbrains.ext

internal inline fun <T> List<T>.forEachIndexed(
    range: IntRange = indices,
    startIndexAtZero: Boolean = false,
    action: (index: Int, item: T) -> Unit,
) {
    var index = if (startIndexAtZero) 0 else range.first
    for (i in range) {
        action(index, get(index = i))
        index++
    }
}

internal inline fun <T> List<T>.forEach(
    range: IntRange = indices,
    action: (item: T) -> Unit,
) {
    for (i in range) {
        action(get(index = i))
    }
}

internal inline fun <T, O> List<T>.flatMapNotNull(
    transform: (T) -> List<O>?,
): List<O> {
    val output = mutableListOf<O>()
    for (item in this) {
        transform(item)?.also { output += it }
    }
    return output
}
