package dev.henkle.surreal.test.models

import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.nanoId
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    override val id: Thing.ID<Person> = nanoId(Person),
    val firstName: String,
    val lastName: String,
    val country: Thing<Country>,
    val series: Thing<Series>,
) : SurrealRecord<Person> {
    override val tableName: String get() = Companion.tableName
    companion object : SurrealTable<Person> {
        override val tableName: String = "person"
    }
}
