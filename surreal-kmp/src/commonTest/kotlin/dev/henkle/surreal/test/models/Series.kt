package dev.henkle.surreal.test.models

import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.nanoId
import kotlinx.serialization.Serializable

@Serializable
data class Series(
    override val id: Thing.ID<Series> = nanoId(Series),
    val name: String,
    val primaryMedium: Thing<Medium>,
    val genre: Thing<Genre>,
) : SurrealRecord<Series> {
    override val tableName: String get() = Companion.tableName
    companion object : SurrealTable<Series> {
        override val tableName: String = "series"
    }
}
