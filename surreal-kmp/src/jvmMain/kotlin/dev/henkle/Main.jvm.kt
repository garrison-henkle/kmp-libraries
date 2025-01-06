package dev.henkle

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dev.henkle.surreal.BuildKonfig
import dev.henkle.surreal.Surreal
import dev.henkle.surreal.ext.delete
import dev.henkle.surreal.ext.insert
import dev.henkle.surreal.ext.query
import dev.henkle.surreal.ext.queryMany
import dev.henkle.surreal.ext.update
import dev.henkle.surreal.sdk.SurrealConnection
import dev.henkle.surreal.types.SurrealLiveQueryUpdate
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.generateId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

suspend fun main() {
    Logger.setMinSeverity(severity = Severity.Debug)
    val db = try {
        Surreal.create(
            url = "localhost",
            port = 57241,
            connection = SurrealConnection(
                user = BuildKonfig.surrealUsername,
                password = BuildKonfig.surrealPassword,
                namespace = "lightn",
                database = "lightn",
            )
        )
    } catch(ex: Exception) {
        Logger.e("garrison", throwable = ex) { "Failed to open database connection!" }
        return
    }

    val job3: Job? = null
    val job2 = CoroutineScope(Dispatchers.IO).launch {
        for(i in 1..2) {
            delay(timeMillis = 3_000)
            try {
                db.insert(record = Person(firstName = "Person $i", lastName = "Smith"))
            } catch(ex: Exception) {
                Logger.e("garrison", throwable = ex) { "ISSUE??!?!?!" }
            }
        }
        db.shutdown()
        val lastInsertResult = db.insert(record = Person(firstName = "Person -1", lastName = "Smith"))
        Logger.e("garrison") { lastInsertResult.toString() }
        db.start()
        db.connectionStatus.firstOrNull { it is Surreal.ConnectionStatus.Connected }
        val lastInsertResult2 = db.insert(record = Person(firstName = "Person -2", lastName = "Smith"))
        Logger.e("garrison") { lastInsertResult2.toString() }
        db.shutdown()
        val lastInsertResult3 = db.insert(record = Person(firstName = "Person -3", lastName = "Smith"))
        Logger.e("garrison") { lastInsertResult3.toString() }
    }

    job2.join()
}

@Serializable
data class Person(
    override val id: Thing.ID<Person> = generateId(),
    val firstName: String,
    val lastName: String,
) : SurrealRecord<Person> {
    override val tableName: String get() = Companion.tableName
    companion object : SurrealTable<Person> {
        override val tableName: String = "person"
    }
}
