package dev.henkle.surreal.test.models

import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.nanoId
import kotlinx.serialization.Serializable

@Serializable
data class Genre(
    override val id: Thing.ID<Genre> = nanoId(Genre),
    val name: String,
) : SurrealRecord<Genre> {
    override val tableName: String get() = Companion.tableName
    companion object : SurrealTable<Genre> {
        override val tableName: String = "genre"
    }
}
