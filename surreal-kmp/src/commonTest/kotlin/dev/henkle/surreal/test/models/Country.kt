package dev.henkle.surreal.test.models

import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.nanoId
import kotlinx.serialization.Serializable

@Serializable
data class Country(
    override val id: Thing.ID<Country> = nanoId(Country),
    val name: String,
) : SurrealRecord<Country> {
    override val tableName: String get() = Companion.tableName
    companion object : SurrealTable<Country> {
        override val tableName: String = "country"
    }
}
