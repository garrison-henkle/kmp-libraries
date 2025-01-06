package dev.henkle.surreal.errors

sealed interface SurrealError {
    data class SDK(val ex: Throwable) : SurrealError
    data class DB(val error: DatabaseError) : SurrealError
}
