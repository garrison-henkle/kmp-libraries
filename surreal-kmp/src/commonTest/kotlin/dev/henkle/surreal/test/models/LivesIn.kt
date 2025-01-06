package dev.henkle.surreal.test.models

import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.nanoId
import kotlinx.serialization.Serializable

@Serializable
data class LivesIn(
    override val id: Thing.EdgeID<LivesIn, Person, Country> = nanoId(LivesIn),
    override val `in`: Thing<Person>,
    override val out: Thing<Country>,
    val durationInYears: Int,
) : SurrealEdge<LivesIn, Person, Country> {
    override val tableName: String = Companion.tableName
    companion object : SurrealEdgeTable<LivesIn, Person, Country> {
        override val tableName: String = "livesIn"
    }
}
