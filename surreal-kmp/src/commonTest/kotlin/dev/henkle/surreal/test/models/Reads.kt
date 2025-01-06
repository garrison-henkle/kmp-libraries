package dev.henkle.surreal.test.models

import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.nanoId
import kotlinx.serialization.Serializable

@Serializable
data class Reads(
    override val id: Thing.EdgeID<Reads, Person, Medium> = nanoId(Reads),
    override val `in`: Thing<Person>,
    override val out: Thing<Medium>,
) : SurrealEdge<Reads, Person, Medium> {
    override val tableName: String = Companion.tableName
    companion object : SurrealEdgeTable<Reads, Person, Medium> {
        override val tableName: String = "reads"
    }
}
