package dev.henkle.compose.paging

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.CompletableDeferred

class PagedLazyColumnController<T> {
    private var bound = atomic(CompletableDeferred<PagerAdapter<T>>())
    private var pager: PagerAdapter<T>? = null

    internal fun bind(pager: PagerAdapter<T>) {
        this.pager = pager
        bound.update { it.complete(pager); it }
    }

    internal fun unbind() {
        bound.update { CompletableDeferred() }
        this.pager = null
    }

    fun tryClear() {
        pager?.clear()
    }

    suspend fun clear() {
        bound.value.await().clear()
    }

    fun tryRefresh() {
        pager?.refresh()
    }

    suspend fun refresh() {
        bound.value.await().refresh()
    }

    fun tryShutdown() {
        pager?.shutdown()
    }

    suspend fun shutdown() {
        bound.value.await().shutdown()
    }

    fun tryRestart() {
        pager?.startFlows()
    }

    suspend fun restart() {
        bound.value.await().startFlows()
    }

    suspend fun tryLoadNext() {
        pager?.loadNext()
    }

    suspend fun loadNext() {
        bound.value.await().loadNext()
    }

    suspend fun tryLoadPrevious() {
        pager?.loadPrevious()
    }

    suspend fun loadPrevious() {
        bound.value.await().loadPrevious()
    }

    fun tryDelete(item: T): Boolean = pager?.delete(item = item) ?: false

    suspend fun delete(item: T): Boolean = bound.value.await().delete(item = item)

    fun tryUpdate(item: T, newValue: T): Boolean = pager?.update(item = item, newValue = newValue) ?: false

    suspend fun update(item: T, newValue: T): Boolean = bound.value.await().update(item = item, newValue = newValue)
}
