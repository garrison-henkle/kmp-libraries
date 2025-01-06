package dev.henkle.surreal.test.models

import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.nanoId
import kotlinx.serialization.Serializable

@Serializable
data class Company(
    override val id: Thing.ID<Company> = nanoId(Company),
    val name: String,
) : SurrealRecord<Company> {
    override val tableName: String = Companion.tableName
    companion object : SurrealTable<Company> {
        override val tableName: String = "company"
    }
}
