package dev.henkle.compose.paging

import com.benasher44.uuid.uuid4

sealed interface Page<T> {
    val page: Int
    val size: Int
    val data: List<T>
    val isFull: Boolean get() = size == data.size

    fun copy(data: List<T>, size: Int = data.size): Page<T>

    fun copyWithNewID(data: List<T>, size: Int = data.size): Page<T>

    data class OffsetPage<T>(
        val uuid: String = uuid4().toString(),
        override val page: Int,
        val offset: Int,
        override val size: Int,
        override val data: List<T>,
    ) : Page<T> {
        override fun equals(other: Any?): Boolean =
            when (other) {
                is OffsetPage<*> -> this.uuid == other.uuid
                else -> false
            }

        override fun hashCode(): Int {
            var result = uuid.hashCode()
            result = 31 * result + page
            result = 31 * result + offset
            result = 31 * result + size
            result = 31 * result + data.hashCode()
            return result
        }

        override fun copy(data: List<T>, size: Int): Page<T> =
            OffsetPage(
                uuid = this.uuid,
                page = this.page,
                offset = this.offset,
                size = size,
                data = data,
            )

        override fun copyWithNewID(data: List<T>, size: Int): Page<T> =
            OffsetPage(
                uuid = uuid4().toString(),
                page = this.page,
                offset = this.offset,
                size = size,
                data = data,
            )
    }

    data class IDPage<T, ID>(
        val uuid: String = uuid4().toString(),
        override val page: Int,
        val previousPageLastID: ID?,
        override val size: Int,
        override val data: List<T>,
    ) : Page<T> {
        override fun equals(other: Any?): Boolean =
            when (other) {
                is IDPage<*, *> -> this.uuid == other.uuid
                else -> false
            }

        override fun hashCode(): Int {
            var result = uuid.hashCode()
            result = 31 * result + page
            result = 31 * result + (previousPageLastID?.hashCode() ?: 0)
            result = 31 * result + size
            result = 31 * result + data.hashCode()
            return result
        }

        override fun copy(data: List<T>, size: Int): Page<T> =
            IDPage(
                uuid = this.uuid,
                page = this.page,
                previousPageLastID = this.previousPageLastID,
                data = data,
                size = size,
            )

        override fun copyWithNewID(data: List<T>, size: Int): Page<T> =
            IDPage(
                uuid = uuid4().toString(),
                page = this.page,
                previousPageLastID = this.previousPageLastID,
                data = data,
                size = size,
            )
    }
}
