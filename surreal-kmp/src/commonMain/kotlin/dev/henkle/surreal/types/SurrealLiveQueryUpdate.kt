package dev.henkle.surreal.types

sealed interface SurrealLiveQueryUpdate<T> {
    data class Create<T>(val record: T) : SurrealLiveQueryUpdate<T>
    data class Update<T>(val record: T) : SurrealLiveQueryUpdate<T>
    data class Delete<T>(val id: Thing.ID<*>) : SurrealLiveQueryUpdate<T>
}
