package dev.henkle.surreal.test.models

import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.nanoId
import kotlinx.serialization.Serializable

@Serializable
data class Medium(
    override val id: Thing.ID<Medium> = nanoId(Medium),
    val name: String,
) : SurrealRecord<Medium> {
    override val tableName: String get() = Companion.tableName
    companion object : SurrealTable<Medium> {
        override val tableName: String = "medium"
    }
}