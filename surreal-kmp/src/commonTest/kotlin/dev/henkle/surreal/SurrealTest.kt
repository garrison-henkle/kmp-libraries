package dev.henkle.surreal

import dev.henkle.surreal.errors.EmptyArgListException
import dev.henkle.surreal.errors.NonSingleRecordResultException
import dev.henkle.surreal.errors.NonSingleStatementQueryException
import dev.henkle.surreal.errors.QueryResultCountMismatchException
import dev.henkle.surreal.errors.SurrealError
import dev.henkle.surreal.ext.delete
import dev.henkle.surreal.ext.deleteAll
import dev.henkle.surreal.ext.get
import dev.henkle.surreal.ext.getAll
import dev.henkle.surreal.ext.insert
import dev.henkle.surreal.ext.live
import dev.henkle.surreal.ext.merge
import dev.henkle.surreal.ext.mergeAll
import dev.henkle.surreal.ext.put
import dev.henkle.surreal.ext.query
import dev.henkle.surreal.ext.queryMany
import dev.henkle.surreal.ext.queryOne
import dev.henkle.surreal.ext.relate
import dev.henkle.surreal.ext.update
import dev.henkle.surreal.ext.updateAll
import dev.henkle.surreal.internal.impl.SurrealImpl
import dev.henkle.surreal.internal.utils.IO
import dev.henkle.surreal.internal.utils.nullSerializer
import dev.henkle.surreal.sdk.RawSurrealStatementResult
import dev.henkle.surreal.sdk.SurrealConnection
import dev.henkle.surreal.sdk.SurrealLiveQueryResponse
import dev.henkle.surreal.sdk.SurrealQueryResult
import dev.henkle.surreal.sdk.SurrealResult
import dev.henkle.surreal.test.AfterClass
import dev.henkle.surreal.test.BeforeClass
import dev.henkle.surreal.test.TestData
import dev.henkle.surreal.test.TestData.candy
import dev.henkle.surreal.test.TestData.comedy
import dev.henkle.surreal.test.TestData.countries
import dev.henkle.surreal.test.TestData.drama
import dev.henkle.surreal.test.TestData.fantasy
import dev.henkle.surreal.test.TestData.garrison
import dev.henkle.surreal.test.TestData.genres
import dev.henkle.surreal.test.TestData.lightNovel
import dev.henkle.surreal.test.TestData.manga
import dev.henkle.surreal.test.TestData.pact
import dev.henkle.surreal.test.TestData.people
import dev.henkle.surreal.test.TestData.sanMagnolia
import dev.henkle.surreal.test.TestData.sciFi
import dev.henkle.surreal.test.TestData.spike
import dev.henkle.surreal.test.TestData.us
import dev.henkle.surreal.test.models.Company
import dev.henkle.surreal.test.models.Country
import dev.henkle.surreal.test.models.FirstName
import dev.henkle.surreal.test.models.Genre
import dev.henkle.surreal.test.models.LivesIn
import dev.henkle.surreal.test.models.Medium
import dev.henkle.surreal.test.models.Name
import dev.henkle.surreal.test.models.Person
import dev.henkle.surreal.test.models.Reads
import dev.henkle.surreal.types.SurrealLiveQueryUpdate
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import dev.henkle.surreal.utils.id
import dev.henkle.surreal.utils.nanoId
import dev.henkle.test.Process
import dev.henkle.test.TestSuite
import dev.henkle.test.assert
import dev.henkle.test.assertEq
import dev.henkle.test.executeCommand
import dev.henkle.test.printlnToStdErr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlin.js.JsName
import kotlin.jvm.JvmStatic
import kotlin.test.BeforeTest
import kotlin.test.Test

class SurrealTest {
    companion object {
        private lateinit var suite: TestSuite
        private lateinit var db: SurrealImpl

        private const val SURREAL_HOST = "localhost"
        private const val SURREAL_PORT = 46356
        private const val USER = "root"
        private const val PASS = "password"
        private const val SURREAL_NS = "surrealkmp"
        private const val SURREAL_DB = "test"

        private val START_COMMAND = """
            surreal start -u "$USER" -p "$PASS" -b 0.0.0.0:$SURREAL_PORT
        """.trimIndent()

        private val SETUP_SQL = listOf(
            "define table if not exists person;",
            "define table if not exists series;",
            "define table if not exists country;",
            "define table if not exists medium;",
            "define table if not exists genre;",
            "delete person;",
            "delete series;",
            "delete country;",
            "delete medium;",
            "delete genre;",
            "delete company;",
            "delete reads;",
            "delete livesIn;",
            *TestData.asSQLInsertStatements(),
        ).joinToString(separator = " ")
            .replace(oldValue = "$", newValue = "\\$")
            .replace(oldValue = "\"", newValue = "\\\"")

        private val SURREAL_SQL_COMMAND = """
            echo "%s" | \
            surreal sql \
            -e http://$SURREAL_HOST:$SURREAL_PORT \
            -u "$USER" \
            -p "$PASS" \
            --ns $SURREAL_NS \
            --db $SURREAL_DB
        """.trimIndent()

        private lateinit var surrealDBProcess: Process

        private fun executeSurrealQL(sql: String): String {
            return executeCommand(command = SURREAL_SQL_COMMAND.replaceFirst(oldValue = "%s", newValue = sql)).apply {
                if(code != 0) {
                    printlnToStdErr("SQL failure:\n$stderr")
                }
            }.stdout
        }

        @Suppress("unused")
        @BeforeClass
        @JvmStatic
        fun setup() {
            executeCommand(command = "kill -9 $(lsof -ti:$SURREAL_PORT)")
            surrealDBProcess = Process(command = START_COMMAND)
            suite = TestSuite().setup {
                surrealDBProcess.output.firstOrNull { "Started web server on" in it }
                db = SurrealImpl.create(
                    url = SURREAL_HOST,
                    port = SURREAL_PORT,
                    connection = SurrealConnection(
                        user = USER,
                        password = PASS,
                        namespace = SURREAL_NS,
                        database = SURREAL_DB,
                    ),
                    context = dispatcher,
                )
            }
        }

        @Suppress("unused")
        @AfterClass
        @JvmStatic
        fun cleanUp() = suite.runTest {
            db.shutdown()
            surrealDBProcess.kill()
        }
    }

    @BeforeTest
    fun beforeEach() {
        executeSurrealQL(SETUP_SQL)
    }

    // GIVEN the id of a record in the database
    // WHEN get is called with the id
    // THEN the record is returned
    @JsName("test1")
    @Test
    fun `get fetches a record from the table by id`() = suite.runTest {
        val expectedPerson = garrison

        val personById = db.get(id = garrison.id).value
        val personByRecord = db.get(id = garrison).value

        assertEq(
            expected = expectedPerson,
            actual = personById,
        ) {
            "The get() should have retrieved the record from the database by the provided id!"
        }

        assertEq(
            expected = expectedPerson,
            actual = personByRecord,
        ) {
            "The get() should have retrieved the record from the database by the provided record's id!"
        }
    }

    // GIVEN the id of a record that does not exist in the database
    // WHEN get is called with the id
    // THEN null is returned
    @JsName("test2")
    @Test
    fun `get returns null when no record with the provided id exists in the table`() =
        suite.runTest {
            val id = id(table = Genre, id = "action")
            val record = Genre(name = "Action")

            val genreById = db.get(id = id)
            val genreByRecord = db.get(id = record)

            assert(genreById is SurrealResult.Success && genreById.value == null) {
                "The get() should return null when given an id that does not exist in the database!"
            }

            assert(genreByRecord is SurrealResult.Success && genreByRecord.value == null) {
                "The get() should return null when given a record that does not exist in the database!"
            }
        }

    // GIVEN the ids of several records in the database
    // WHEN get is called with the ids
    // THEN the records are returned
    @JsName("test3")
    @Test
    fun `get with multiple ids fetches all the records with matching ids from the table`() =
        suite.runTest {
            val expectedCountries = setOf(candy, pact, sanMagnolia)

            val countriesByIds = db.get(ids = expectedCountries.map { it.id }).value?.toSet()
            val countriesByRecord = db.get(ids = expectedCountries).value?.toSet()

            assertEq(
                expected = expectedCountries,
                actual = countriesByIds,
            ) {
                "The get() should have retrieved the records with the provided ids from the database!"
            }

            assertEq(
                expected = expectedCountries,
                actual = countriesByRecord,
            ) {
                "The get() should have retrieved the records with the provided records' ids from the database!"
            }
        }

    // GIVEN the ids of several records where all but one are in the database
    // WHEN get is called with the ids
    // THEN all the records are returned besides for the one that was not in the database
    @JsName("test4")
    @Test
    fun `get with multiple ids where some do not exist in the table fetches only the records that exist in the table`() =
        suite.runTest {
            val france = Country(name = "French Republic")
            val countries = listOf(candy, pact, france)
            val expectedCountries = setOf(candy, pact)

            val countriesByIds = db.get(ids = countries.map { it.id }).value?.toSet()
            val countriesByRecord = db.get(ids = countries).value?.toSet()

            assertEq(
                expected = expectedCountries,
                actual = countriesByIds,
            ) {
                "The get() should have retrieved the records with matching ids!"
            }

            assertEq(
                expected = expectedCountries,
                actual = countriesByRecord,
            ) {
                "The get() should have retrieved the records with ids matching the provided records' ids!"
            }
        }

    // GIVEN the ids of several records where none of the records exist in the database
    // WHEN get is called with the ids
    // THEN an empty list is returned
    @JsName("test5")
    @Test
    fun `get with multiple ids where all do not exist in the table results in an empty list`() =
        suite.runTest {
            val france = Country(name = "French Republic")
            val uk = Country(name = "United Kingdom of Great Britain and Northern Ireland")
            val germany = Country(name = "Federal Republic of Germany")
            val countries = listOf(uk, france, germany)

            val countriesByIds = db.get(ids = countries.map { it.id })
            val countriesByRecord = db.get(ids = countries)

            assert(countriesByIds is SurrealResult.Success && countriesByIds.value.isEmpty()) {
                "The get() should have retrieved no records because none of the ids should exist in the database!"
            }

            assert(countriesByRecord is SurrealResult.Success && countriesByRecord.value.isEmpty()) {
                "The get() should have retrieved no records because none of the records' ids should exist in the database!"
            }
        }

    // GIVEN a table with records
    // WHEN getAll is called with the table
    // THEN all the records in the table are returned
    @JsName("test6")
    @Test
    fun `getAll fetches all records in the table`() =
        suite.runTest {
            val expectedGenres = genres.toSet()

            val genres = db.getAll(table = Genre).value?.toSet()

            assertEq(
                expected = expectedGenres,
                actual = genres,
            ) {
                "The getAll() didn't retrieve all the records in the genre table!"
            }
        }

    // GIVEN a table with no records
    // WHEN getAll is called with the table
    // THEN an empty list is returned
    @JsName("test7")
    @Test
    fun `getAll returns an empty list if the table contains no records`() =
        suite.runTest {
            @Serializable
            data class Volume(override val id: Thing.ID<Volume>) : SurrealRecord<Volume>
            val volumesTable = object : SurrealTable<Volume> { override val tableName: String = "volume" }

            val volumes = db.getAll(table = volumesTable)

            assert(volumes is SurrealResult.Success && volumes.value.isEmpty()) {
                "getAll() should return an empty list if the table does not contain records!"
            }
        }

    // GIVEN a record that does not exist in the database
    // WHEN insert is called on the record
    // THEN it is successfully inserted and returned
    @JsName("test8")
    @Test
    fun `inserting a record that does not exist in the database succeeds`() =
        suite.runTest {
            val uk = Country(name = "United Kingdom of Great Britain and Northern Ireland")

            val result = db.insert(record = uk)

            assert(result is SurrealResult.Success) {
                "The insertion should have succeeded!"
            }

            assertEq(
                expected = uk,
                actual = result.value,
            ) {
                "The country returned by the insertion did not match what was supposed to be inserted!"
            }
        }

    // GIVEN a record that exists in the database
    // WHEN insert is called on the record
    // THEN it is not inserted and a database error is returned
    @JsName("test9")
    @Test
    fun `inserting a record that exists in the database fails`() =
        suite.runTest {
            val error = db.insert(record = garrison).error

            assert(error is SurrealError.DB && error.error.isCollision) {
                "The insertion should have failed due to a collision!"
            }
        }

    // GIVEN an empty list of records
    // WHEN insert is called with the list
    // THEN a Failure result is returned with an IllegalArgumentException
    @JsName("test10")
    @Test
    fun `insert fails with an EmptyArgListException when provided an empty list of records`() =
        suite.runTest {
            val result = db.insert(records = emptyList<Genre>())

            assert(
                result is SurrealResult.Failure &&
                result.error is SurrealError.SDK &&
                (result.error as SurrealError.SDK).ex is EmptyArgListException
            ) {
                "The insertion should have failed with an EmptyArgListException due to the empty records list!"
            }
        }

    // GIVEN records that do not exist in the database
    // WHEN insert is called on the records
    // THEN all the records are successfully inserted and returned
    @JsName("test11")
    @Test
    fun `insert successfully inserts multiple records`() =
        suite.runTest {
            val action = Genre(name = "Action")
            val adventure = Genre(name = "Adventure")
            val expected = setOf(action, adventure)

            val actual = db.insert(records = expected).value?.toSet()

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The records should have been inserted!"
            }
        }

    // GIVEN records where all but one exist in the database
    // WHEN insert is called on the records
    // THEN a Failure result is returned with a database error and no records are inserted
    @JsName("test12")
    @Test
    fun `insert with some records that exist in the database and some that do not will not insert any records`() =
        suite.runTest {
            val action = Genre(name = "Action")

            val expectedGenres = db.getAll(Genre).value
            val error = db.insert(records = setOf(action, fantasy)).error
            val actualGenres = db.getAll(Genre).value

            assert(
                error != null &&
                error is SurrealError.DB &&
                error.error.isCollision
            ) {
                "The insert should have failed due to a collision!"
            }

            assertEq(
                expected = expectedGenres,
                actual = actualGenres,
            ) {
                "The insertion should not have changed the database!"
            }
        }

    // GIVEN a record that does not exist in the database
    // WHEN put is called on the record
    // THEN it is successfully inserted and returned
    @JsName("test13")
    @Test
    fun `put inserts records that do not exist in the database`() =
        suite.runTest {
            val action = Genre(name = "Action")
            val expected = genres.toSet() + action

            val result = db.put(record = action)
            val actual = db.getAll(table = Genre).value?.toSet()

            assert(result is SurrealResult.Success && result.value == action) {
                "The put should have returned successfully with the record!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The Action genre should have been inserted into the database!"
            }
        }

    // GIVEN a record that exists in the database
    // WHEN put is called on the record
    // THEN the record in the database is successfully updated and returned
    @JsName("test14")
    @Test
    fun `put updates records that exist in the database`() =
        suite.runTest {
            val expected = spike.copy(firstName = "spike")

            val result = db.put(record = expected).value
            val actual = db.get(id = expected).value

            assertEq(
                expected = expected,
                actual = result,
            ) {
                "The record returned by the database should have been the provided record!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The record should have been updated in the database!"
            }
        }

    // GIVEN a record that does not exist in the database
    // WHEN update is called on the record
    // THEN the operation no-ops and the database is not altered
    @JsName("test15")
    @Test
    fun `update no-ops when given a record that does not exist in the database`() =
        suite.runTest {
            val france = Country(name = "France")

            val result = db.update(record = france)
            val actual = db.get(id = france).value

            assert(result is SurrealResult.Success && result.value == null) {
                "The update operation should have completed successfully and returned null!"
            }

            assert(actual == null) {
                "The record shouldn't have been inserted into the database!"
            }
        }

    // GIVEN a record that exists in the database
    // WHEN update is called on the record
    // THEN the record is successfully updated and returned
    @JsName("test16")
    @Test
    fun `update updates a record that exists in the database`() =
        suite.runTest {
            val expected = us.copy(name = "USA")

            val result = db.update(record = expected).value
            val actual = db.get(id = expected).value

            assertEq(
                expected = expected,
                actual = result,
            ) {
                "The record returned by the database should have been the provided record!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The record should have been updated in the database!"
            }
        }

    // GIVEN a table with records and an update
    // WHEN updateAll is called on the table with the update
    // THEN all the records in the table are updated
    @JsName("test17")
    @Test
    fun `updateAll replaces all records in the table with the update`() =
        suite.runTest {
            val updatedName = "Adventure"
            val update = Name(name = updatedName)
            val expected = genres.map { it.copy(name = updatedName) }.toSet()

            val actual = db.updateAll(table = Genre, update = update).value?.toSet()

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "All the records in the Genre table should have been updated to have the name '$updatedName'!"
            }
        }

    // GIVEN a table with no records and an update
    // WHEN updateAll is called on the table with the update
    // THEN the operation no-ops and the database is not altered
    @JsName("test18")
    @Test
    fun `updateAll no-ops when called on an empty table`() =
        suite.runTest {
            val update = Company(name = "Google")
            val expected = emptyList<Company>()

            val result = db.updateAll(table = Company, update = update).value
            val actual = db.getAll(table = Company).value

            assertEq(
                expected = expected,
                actual = result,
            ) {
                "No records should have been updated!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The Company table should still be empty!"
            }
        }

    // GIVEN a record that does not exist in the database and an update
    // WHEN merge is called on the record
    // THEN then the merge no-ops and the database is not altered
    @JsName(name = "test19")
    @Test
    fun `merge creates a record with the provided id and the contents of the update if a record with the id does not exist`() =
        suite.runTest {
            val id = nanoId(Genre)
            val name = "Adventure"
            val update = Name(name = name)
            val expected: Genre? = null

            val result = db.merge(id = id, update = update).value
            val actual = db.get(id = id).value

            assertEq(
                expected = expected,
                actual = result,
            ) {
                "The merge should have failed and returned null!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "A record shouldn't have been created in the database!"
            }
        }

    // GIVEN a record that exists in the database and an update
    // WHEN merge is called on the record
    // THEN the record is updated in the database without changes to fields that were not specified in the update
    @JsName(name = "test20")
    @Test
    fun `merge updates a record in the database with the update without touching fields that were not in the update`() =
        suite.runTest {
            val updatedName = "Garry"
            val update = FirstName(firstName = updatedName)
            val expected = garrison.copy(firstName = updatedName)

            val result = db.merge(id = garrison, update = update).value
            val actual = db.get(id = garrison).value

            assertEq(
                expected = expected,
                actual = result,
            ) {
                "The record returned by the database should have been the provided record with the update applied!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The merge should have updated the record!"
            }
        }

    // GIVEN a table with records and an update
    // WHEN mergeAll is called on the table with the update
    // THEN all the records in the table are updated without changes to fields that were not specified in the update
    @JsName(name = "test21")
    @Test
    fun `mergeAll updates records in a table with the update without touching fields that were not in the update`() =
        suite.runTest {
            val updatedName = "name"
            val update = FirstName(firstName = updatedName)
            val expected = people.map { it.copy(firstName = updatedName) }.toSet()

            val result = db.mergeAll(table = Person, update = update).value?.toSet()
            val actual = db.getAll(table = Person).value?.toSet()

            assertEq(
                expected = expected,
                actual = result,
            ) {
                "The result of the mergeAll should have been a list of Person with the update applied!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The merge operation was not properly applied to all records in the table!"
            }
        }

    // GIVEN a table with no records and an update
    // WHEN mergeAll is called on the table with the update
    // THEN the operation no-ops and the database is not altered
    @JsName(name = "test22")
    @Test
    fun `mergeAll no-ops when called on a table with no records`() =
        suite.runTest {
            val update = Name(name = "Google")
            val expected = emptyList<Company>()

            val result = db.mergeAll(table = Company, update = update).value
            val actual = db.getAll(table = Company).value

            assertEq(
                expected = expected,
                actual = result,
            ) {
                "The mergeAll should have no-oped and returned an empty list!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The table should not have had any records inserted into it!"
            }
        }

    // TODO(garrison): create tests that compare update/updateAll and merge/mergeAll e.g. have an optional field that has a value and
    // TODO: show that an update without the field will cause it to be null while the merge will leave its current value

    // GIVEN a record id that exists in the database
    // WHEN delete is called on that id
    // THEN the record is removed from the database
    @JsName(name = "test23")
    @Test
    fun `delete removes an existing record with a matching id from the database`() =
        suite.runTest {
            val recordToDelete = fantasy
            val expected = genres.toSet() - recordToDelete

            val result = db.delete(id = recordToDelete).value
            val actual = db.getAll(table = Genre).value?.toSet()

            assertEq(
                expected = recordToDelete,
                actual = result,
            ) {
                "The deleted record should have been returned!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The table should not longer contain the fantasy genre!"
            }
        }

    // GIVEN a record id that does not exist in the database
    // WHEN delete is called on that id
    // THEN the operation no-ops and the database is not altered
    @JsName(name = "test24")
    @Test
    fun `delete no-ops when called with an id that does not exist in the database`() =
        suite.runTest {
            val recordToDelete = Genre(name = "Action")
            val expected = genres.toSet()

            val result = db.delete(id = recordToDelete).value
            val actual = db.getAll(table = Genre).value?.toSet()

            assertEq(
                expected = null,
                actual = result,
            ) {
                "No record should have been deleted!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "Nothing should have been deleted from the table!"
            }
        }

    // GIVEN record ids that exist in the database
    // WHEN delete is called with the id
    // THEN all the records are removed from the database
    @JsName(name = "test25")
    @Test
    fun `delete removes existing records with matching ids from the database`() =
        suite.runTest {
            val recordsToDelete = setOf(fantasy, drama)
            val expected = genres.toSet() - recordsToDelete

            val result = db.delete(ids = recordsToDelete).value?.toSet()
            val actual = db.getAll(table = Genre).value?.toSet()

            assertEq(
                expected = recordsToDelete,
                actual = result,
            ) {
                "delete should have returned the two records that were supposed to be deleted!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The table contains records that should have been deleted!"
            }
        }

    // GIVEN several record ids where one does not exist in the database
    // WHEN delete is called with the ids
    // THEN all the records with matching ids are removed from the database
    @JsName(name = "test26")
    @Test
    fun `delete removes existing records with matching ids from the database and ignores ids that do not exist in the database`() =
        suite.runTest {
            val action = Genre(name = "Action")
            val expectedDeletedRecords = setOf(fantasy, drama)
            val recordsToDelete = expectedDeletedRecords + action
            val expected = genres.toSet() - recordsToDelete

            val result = db.delete(ids = recordsToDelete).value?.toSet()
            val actual = db.getAll(table = Genre).value?.toSet()

            assertEq(
                expected = expectedDeletedRecords,
                actual = result,
            ) {
                "delete should have returned the two records that were supposed to be deleted!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The table contains records that should have been deleted!"
            }
        }

    // GIVEN record ids that do not exist in the database
    // WHEN delete is called with the ids
    // THEN the operation no-ops and the database is not altered
    @JsName(name = "test27")
    @Test
    fun `delete no-ops if none of the provided ids exist in the database`() =
        suite.runTest {
            val action = Genre(name = "Action")
            val adventure = Genre(name = "Adventure")
            val expectedDeletedRecords = emptyList<Genre>()
            val recordsToDelete = listOf(action, adventure)
            val expected = genres.toSet()

            val result = db.delete(ids = recordsToDelete).value
            val actual = db.getAll(table = Genre).value?.toSet()

            assertEq(
                expected = expectedDeletedRecords,
                actual = result,
            ) {
                "delete should have returned no records because none of the input records exist in the database!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "No records should have been deleted from the database!"
            }
        }

    // GIVEN a table with records
    // WHEN deleteAll is called on the table
    // THEN all the table's records are removed from the database
    @JsName(name = "test28")
    @Test
    fun `deleteAll deletes all records in a table`() =
        suite.runTest {
            val expected = emptyList<Genre>()
            val expectedDeletedRecords = genres.toSet()

            val result = db.deleteAll(table = Genre).value?.toSet()
            val actual = db.getAll(table = Genre).value

            assertEq(
                expected = expectedDeletedRecords,
                actual = result,
            ) {
                "deleteAll should have returned all the records in the database!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "All of the records in the genre table should have been deleted!"
            }
        }

    // GIVEN a table with no records
    // WHEN deleteAll is called on the table
    // THEN the operation no-ops and the database is not altered
    @JsName(name = "test29")
    @Test
    fun `deleteAll no-ops when called on an empty table`() =
        suite.runTest {
            val expected = emptyList<Company>()

            val result = db.deleteAll(table = Company).value
            val actual = db.getAll(table = Company).value

            assertEq(
                expected = expected,
                actual = result,
            ) {
                "No records should have been deleted!"
            }

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The table should still be empty!"
            }
        }

    // GIVEN a query that selects no records
    // WHEN queryOne is called with the query
    // THEN a Failure result is returned containing a NonSingleRecordResultException
    @JsName(name = "test30")
    @Test
    fun `queryOne fails when the query returns no records`() =
        suite.runTest {
            val query = "select * from company"

            val result = db.queryOne<Company>(query = query)

            assert(result.error is SurrealError.SDK) {
                "queryOne should fail when no records are found!"
            }

            assert((result.error as? SurrealError.SDK)?.ex is NonSingleRecordResultException) {
                "queryOne should return a NonSingleRecordResultException when the result of its query contains no records!"
            }
        }

    // GIVEN a query that selects all the fields of a single record
    // WHEN queryOne is called with the query
    // THEN the record is returned
    @JsName(name = "test31")
    @Test
    fun `queryOne succeeds when the query returns a single record`() =
        suite.runTest {
            val query = "select * from genre where id = \$id"
            val expected = fantasy

            val actual = db.queryOne<Genre>(query = query, parameters = mapOf("id" to expected.id)).value

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "queryOne should successfully return the record of a query that results in a single record!"
            }
        }

    // GIVEN a query that selects all the fields of multiple records
    // WHEN queryOne is called with the query
    // THEN a Failure result is returned containing a NonSingleRecordResultException
    @JsName(name = "test32")
    @Test
    fun `queryOne fails when the query returns more than one record`() =
        suite.runTest {
            val query = "select * from person"

            val result = db.queryOne<Person>(query = query)

            assert(result.error is SurrealError.SDK) {
                "queryOne should fail when more than one record is found!"
            }

            assert((result.error as? SurrealError.SDK)?.ex is NonSingleRecordResultException) {
                "queryOne should return a NonSingleRecordResultException when the result of its query contains more than one record!"
            }
        }

    // GIVEN a query that projects the fields of a single record
    // WHEN queryOne is called with the query and a container for the projected record
    // THEN the record is projected into the container
    @JsName(name = "test33")
    @Test
    fun `queryOne can project its result`() =
        suite.runTest {
            val id = garrison.idString
            val query1 = "select firstName from person where id = \$id"
            val query2 = "select * from person where id = \$id"
            val expected = FirstName(firstName = garrison.firstName)

            val result1 = db.queryOne<FirstName>(query = query1, parameters = mapOf("id" to id)).value
            val result2 = db.queryOne<FirstName>(query = query2, parameters = mapOf("id" to id)).value

            assertEq(
                expected = expected,
                actual = result1,
            ) {
                "queryOne should be able to collect the projected fields into the specified container!"
            }

            assertEq(
                expected = expected,
                actual = result2,
            ) {
                "queryOne should be able to project the result fields into the specified container!"
            }
        }

    // GIVEN a query containing multiple statements
    // WHEN queryOne is called with the query
    // THEN a Failure result is returned containing a NonSingleStatementQueryException
    @JsName(name = "test34")
    @Test
    fun `queryOne fails when given more than one statement in its query`() =
        suite.runTest {
            val query = "select * from person; delete person;"

            val result = db.queryOne<Person>(query = query)

            assert(result.error is SurrealError.SDK) {
                "queryOne should fail when more than one statement is included in the query!"
            }

            assert((result.error as? SurrealError.SDK)?.ex is NonSingleStatementQueryException) {
                "queryOne should return a NonSingleStatementQueryException when its query contains more than one statement!"
            }
        }

    // GIVEN a query that selects no records
    // WHEN queryMany is called with the query
    // THEN an empty list is returned
    @JsName(name = "test35")
    @Test
    fun `queryMany returns an empty list when its query selects no records`() =
        suite.runTest {
            val expected = emptyList<Genre>()
            val query = "select * from genre where id = \$id"

            val actual = db.queryMany<Genre>(query = query, parameters = mapOf("id" to "42")).value

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The query should have returned no results!"
            }
        }

    // GIVEN a query that selects all the fields of a single record
    // WHEN queryMany is called with the query
    // THEN a list containing the single record is returned
    @JsName(name = "test36")
    @Test
    fun `queryMany returns a list containing a single record when its query selects a single record`() =
        suite.runTest {
            val expected = listOf(sciFi)
            val query = "select * from genre where id = \$id"

            val actual = db.queryMany<Genre>(query = query, parameters = mapOf("id" to sciFi)).value

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The query should have returned a list containing a single Sci-Fi genre!"
            }
        }

    // GIVEN a query that selects all the fields of multiple records
    // WHEN queryMany is called with the query
    // THEN a list containing the records is returned
    @JsName(name = "test37")
    @Test
    fun `queryMany returns a list of multiple records when its query selects more than one record`() =
        suite.runTest {
            val expected = setOf(sciFi, comedy)
            val query = "select * from genre where id in \$ids"

            val actual = db.queryMany<Genre>(query = query, parameters = mapOf("ids" to expected)).value?.toSet()

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "The query should have returned a list containing the Sci-Fi and Comedy genre!"
            }
        }

    // GIVEN a query that projects the fields of several records
    // WHEN queryMany is called with the query and a container for the projected records
    // THEN the record is projected into a list of projection containers
    @JsName(name = "test38")
    @Test
    fun `queryMany can project its results`() =
        suite.runTest {
            val query1 = "select firstName from person"
            val query2 = "select * from person"
            val expected = people.map { FirstName(firstName = it.firstName) }.toSet()

            val result1 = db.queryMany<FirstName>(query = query1).value?.toSet()
            val result2 = db.queryMany<FirstName>(query = query2).value?.toSet()

            assertEq(
                expected = expected,
                actual = result1,
            ) {
                "queryMany should be able to collect the projected fields into the specified containers!"
            }

            assertEq(
                expected = expected,
                actual = result2,
            ) {
                "queryMany should be able to project the result fields into the specified containers!"
            }
        }

    // GIVEN a query containing multiple statements
    // WHEN queryMany is called with the query
    // THEN a Failure result is returned containing a NonSingleStatementQueryException
    @JsName(name = "test39")
    @Test
    fun `queryMany fails when given more than one statement in its query`() =
        suite.runTest {
            val query = "select * from person; delete person;"

            val result = db.queryMany<Person>(query = query)

            assert(result.error is SurrealError.SDK) {
                "queryMany should fail when more than one statement is included in the query!"
            }

            assert((result.error as SurrealError.SDK).ex is NonSingleStatementQueryException) {
                "queryMany should return a NonSingleStatementQueryException when its query contains more than one statement!"
            }
        }

    // GIVEN a blank query
    // WHEN queryRaw is called with the query
    // THEN an empty list of query results is returned
    @JsName(name = "test40")
    @Test
    fun `queryRaw returns an empty list of query results when given a blank query`() =
        suite.runTest {
            val query = ""
            val expected = emptyList<SurrealQueryResult<JsonElement>>()

            val actual = db.queryRaw(query = query).value

            assertEq(
                expected = expected,
                actual = actual,
            ) {
                "No query results should be returned for a query containing no statements!"
            }
        }

    // GIVEN a query with a single statement
    // WHEN queryRaw is called with the query
    // THEN a list containing a single query result is returned
    @JsName(name = "test41")
    @Test
    fun `queryRaw returns a list containing a single query result when given a query with a single statement`() =
        suite.runTest {
            val query = "select * from medium where id = \$id"
            val expected = listOf(nullSerializer.encodeToJsonElement(manga))
            val expectedQueryResultCount = 1

            val result = db.queryRaw(query = query, parameters = mapOf("id" to manga.id)).value

            assertEq(
                expected = expectedQueryResultCount,
                actual = result?.size,
            ) {
                "Only a single query result should be received for a query containing a single statement!"
            }

            assertEq(
                expected = expected,
                actual = result?.firstOrNull()?.result?.data,
            ) {
                "A single JsonElement should be returned containing the result!"
            }
        }

    // GIVEN a query with multiple statements
    // WHEN queryRaw is called with the query
    // THEN a list containing a query result for each statement is returned
    @JsName(name = "test42")
    @Test
    fun `queryRaw returns a list of multiple query results when given a query with a multiple statement`() =
        suite.runTest {
            val query = "select * from medium where id = \$id; select * from genre;"
            val expectedResult1 = listOf(nullSerializer.encodeToJsonElement(manga))
            val expectedResult2 = nullSerializer.encodeToJsonElement(genres).jsonArray.toSet()
            val expectedQueryResultCount = 2

            val result = db.queryRaw(query = query, parameters = mapOf("id" to manga.id)).value

            assertEq(
                expected = expectedQueryResultCount,
                actual = result?.size,
            ) {
                "Two query results should be received for a query containing two statements!"
            }

            assertEq(
                expected = expectedResult1,
                actual = result?.get(index = 0)?.result?.data,
            ) {
                "A single JsonElement should be returned containing the result!"
            }

            assertEq(
                expected = expectedResult2,
                actual = result?.get(index = 1)?.result?.data?.toSet(),
            ) {
                "A single JsonElement should be returned containing the result!"
            }
        }

    @JsName(name = "test43")
    @Test
    fun `relate creates an edge between two records`() =
        suite.runTest {
            val query = "select * from person where ->reads->(medium where name = 'Light Novel')"
            val expectedPerson = garrison
            val expectedMedium = lightNovel

            val actualEdge = db.relate(table = Reads, `in` = expectedPerson, out = expectedMedium).value
            val actualPerson = db.queryOne<Person>(query = query).value

            assert(actualEdge?.idString?.startsWith(prefix = Reads.tableName) == true) {
                "The id of the relation should start with the name of the edge table!"
            }

            assertEq(
                expected = expectedPerson.id,
                actual = actualEdge?.`in`,
            ) {
                "The in record of the relation should be the garrison Person record!"
            }

            assertEq(
                expected = expectedMedium.id,
                actual = actualEdge?.out,
            ) {
                "The out record of the relation should be the lightNovel Medium record!"
            }

            assertEq(
                expected = expectedPerson,
                actual = actualPerson,
            ) {
                "The Person garrison should have been found using the new relation!"
            }
        }

    @JsName(name = "test44")
    @Test
    fun `relate can create edges between two records that contain additional data`() =
        suite.runTest {
            val query = "select * from person where ->livesIn->(country where name = 'United States of America')"
            val expectedPerson = garrison
            val expectedCountry = us
            val expectedYears = 25
            val edge = LivesIn(
                `in` = expectedPerson.id,
                out = expectedCountry.id,
                durationInYears = expectedYears,
            )

            val actualEdge = db.relate(edge = edge).value
            val actualPerson = db.queryOne<Person>(query = query).value

            assert(actualEdge?.idString?.startsWith(prefix = LivesIn.tableName) == true) {
                "The id of the relation should start with the name of the edge table!"
            }

            assertEq(
                expected = expectedPerson.id,
                actual = actualEdge?.`in`,
            ) {
                "The in record of the relation should be the garrison Person record!"
            }

            assertEq(
                expected = expectedCountry.id,
                actual = actualEdge?.out,
            ) {
                "The out record of the relation should be the us Country record!"
            }

            assertEq(
                expected = expectedYears,
                actual = actualEdge?.durationInYears,
            ) {
                "The out record of the relation should be the us Country record!"
            }


            assertEq(
                expected = expectedPerson,
                actual = actualPerson,
            ) {
                "The Person garrison should have been found using the new relation!"
            }
        }

    @JsName("test45")
    @Test
    fun `live starts a live query on the specified table that can be later stopped with kill`() =
        suite.runTest {
            val uk = Country(name = "United Kingdom of Great Britain and Northern Ireland")
            val updatedUK = uk.copy(name = "United Kingdom")
            val france = Country(name = "French Republic")
            val expectedUpdates = listOf(
                SurrealLiveQueryUpdate.Create(record = uk),
                SurrealLiveQueryUpdate.Update(record = updatedUK),
                SurrealLiveQueryUpdate.Delete(id = updatedUK.id),
            )

            val liveResponse = db.live(table = Country).value

            assert(liveResponse != null) { "The live query start request should have succeeded!" }

            val updates = liveResponse.updates.consumeAsFlow().shareIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                replay = 4,
            )

            val inserted = db.insert(record = uk).value

            assertEq(
                expected = uk,
                actual = inserted,
            ) {
                "The record returned by the database insertion should match the provided record!"
            }

            val updated = db.update(record = updatedUK).value

            assertEq(
                expected = updatedUK,
                actual = updated,
            ) {
                "The record returned by the database update should match the provided record!"
            }

            val deleted = db.delete(id = uk).value

            assertEq(
                expected = updatedUK,
                actual = deleted
            ) {
                "The record returned by the database deletion should match the record provided in the prevoius update!"
            }

            val killResult = db.kill(handle = liveResponse.handle)

            assert(killResult.isSuccess) { "The kill should have succeeded" }

            val ignoredInsert = db.insert(record = france).value

            assertEq(
                expected = france,
                actual = ignoredInsert,
            ) {
                "The record returned by the database insertion should match the provided record!"
            }

            advanceUntilIdle()

            val actualUpdates = withContext(Dispatchers.IO) {
                delay(timeMillis = 500)
                advanceUntilIdle()
                updates.replayCache
            }

            assertEq(
                expected = expectedUpdates,
                actual = actualUpdates,
            ) {
                "The insertion, update, and deletion should have been received as live updates and " +
                    "the final insertion should have been ignored!"
            }
        }

    // GIVEN the id of a record in the database
    // WHEN get is called with the id
    // THEN the record is returned
    @JsName("queryBuilderTest1")
    @Test
    fun `query builder get fetches a record from the table by id`() = suite.runTest {
        val expectedPerson = garrison

        val (personById, personByRecord, result) = db.query<Person?, Person?> {
            get(id = garrison.id) to
                get(id = garrison)
        }

        assert(result.isSuccess) {
            "The query should have succeeded"
        }

        assertEq(
            expected = expectedPerson,
            actual = personById.value,
        ) {
            "The get() should have retrieved the record from the database by the provided id!"
        }

        assertEq(
            expected = expectedPerson,
            actual = personByRecord.value,
        ) {
            "The get() should have retrieved the record from the database by the provided record's id!"
        }
    }

    // GIVEN the id of a record that does not exist in the database
    // WHEN get is called with the id
    // THEN null is returned
    @JsName("queryBuilderTest2")
    @Test
    fun `query builder get returns null when no record with the provided id exists in the table`() =
        suite.runTest {
            val id = id(table = Genre, id = "action")
            val record = Genre(name = "Action")

            val (genreById, genreByRecord, result) = db.query<Genre?, Genre?> {
                get(id = id) to get(id = record)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assert(genreById is SurrealResult.Success && genreById.value == null) {
                "The get() should return null when given an id that does not exist in the database!"
            }

            assert(genreByRecord is SurrealResult.Success && genreByRecord.value == null) {
                "The get() should return null when given a record that does not exist in the database!"
            }

        }

    // GIVEN the ids of several records in the database
    // WHEN get is called with the ids
    // THEN the records are returned
    @JsName("queryBuilderTest3")
    @Test
    fun `query builder get with multiple ids fetches all the records with matching ids from the table`() =
        suite.runTest {
            val expectedCountries = setOf(candy, pact, sanMagnolia)

            val (countriesById, countriesByRecord, result) = db.query<List<Country>, List<Country>> {
                get(ids = expectedCountries.map { it.id }) to get(ids = expectedCountries)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expectedCountries,
                actual = countriesById.value?.toSet(),
            ) {
                "The get() should have retrieved the records with the provided ids from the database!"
            }

            assertEq(
                expected = expectedCountries,
                actual = countriesByRecord.value?.toSet(),
            ) {
                "The get() should have retrieved the records with the provided records' ids from the database!"
            }
        }

    // GIVEN the ids of several records where all but one are in the database
    // WHEN get is called with the ids
    // THEN all the records are returned besides for the one that was not in the database
    @JsName("queryBuilderTest4")
    @Test
    fun `query builder get with multiple ids where some do not exist in the table fetches only the records that exist in the table`() =
        suite.runTest {
            val france = Country(name = "French Republic")
            val countries = listOf(candy, pact, france)
            val expectedCountries = setOf(candy, pact)

            val (countriesByIds, countriesByRecord, result) = db.query<List<Country>, List<Country>> {
                get(ids = countries.map { it.id }) to get(ids = countries)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expectedCountries,
                actual = countriesByIds.value?.toSet(),
            ) {
                "The get() should have retrieved the records with matching ids!"
            }

            assertEq(
                expected = expectedCountries,
                actual = countriesByRecord.value?.toSet(),
            ) {
                "The get() should have retrieved the records with ids matching the provided records' ids!"
            }
        }

    // GIVEN the ids of several records where none of the records exist in the database
    // WHEN get is called with the ids
    // THEN an empty list is returned
    @JsName("queryBuilderTest5")
    @Test
    fun `query builder get with multiple ids where all do not exist in the table results in an empty list`() =
        suite.runTest {
            val france = Country(name = "French Republic")
            val uk = Country(name = "United Kingdom of Great Britain and Northern Ireland")
            val germany = Country(name = "Federal Republic of Germany")
            val countries = listOf(uk, france, germany)

            val (countriesByIds, countriesByRecord, result) = db.query<List<Country>, List<Country>> {
                get(ids = countries.map { it.id }) to get(ids = countries)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assert(countriesByIds is SurrealResult.Success && countriesByIds.value.isEmpty()) {
                "The get() should have retrieved no records because none of the ids should exist in the database!"
            }

            assert(countriesByRecord is SurrealResult.Success && countriesByRecord.value.isEmpty()) {
                "The get() should have retrieved no records because none of the records' ids should exist in the database!"
            }
        }

    // GIVEN a table with records
    // WHEN getAll is called with the table
    // THEN all the records in the table are returned
    @JsName("queryBuilderTest6")
    @Test
    fun `query builder getAll fetches all records in the table`() =
        suite.runTest {
            val expectedGenres = genres.toSet()

            val (genres, result) = db.query<List<Genre>> {
                getAll(table = Genre)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expectedGenres,
                actual = genres.value?.toSet(),
            ) {
                "The getAll() didn't retrieve all the records in the genre table!"
            }
        }

    // GIVEN a table with no records
    // WHEN getAll is called with the table
    // THEN an empty list is returned
    @JsName("queryBuilderTest7")
    @Test
    fun `query builder getAll returns an empty list if the table contains no records`() =
        suite.runTest {
            @Serializable
            data class Volume(override val id: Thing.ID<Volume>) : SurrealRecord<Volume>
            val volumesTable = object : SurrealTable<Volume> { override val tableName: String = "volume" }

            val (volumes, result) = db.query<List<Volume>> {
                getAll(table = volumesTable)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assert(volumes is SurrealResult.Success && volumes.value.isEmpty()) {
                "getAll() should return an empty list if the table does not contain records!"
            }
        }

    // GIVEN a record that does not exist in the database
    // WHEN insert is called on the record
    // THEN it is successfully inserted and returned
    @JsName("queryBuilderTest8")
    @Test
    fun `query builder inserting a record that does not exist in the database succeeds`() =
        suite.runTest {
            val uk = Country(name = "United Kingdom of Great Britain and Northern Ireland")

            val (country, result) = db.query<Country> {
                insert(record = uk)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assert(country is SurrealResult.Success) {
                "The insertion should have succeeded!"
            }

            assertEq(
                expected = uk,
                actual = country.value,
            ) {
                "The country returned by the insertion did not match what was supposed to be inserted!"
            }
        }

    // GIVEN a record that exists in the database
    // WHEN insert is called on the record
    // THEN it is not inserted and a database error is returned
    @JsName("queryBuilderTest9")
    @Test
    fun `query builder inserting a record that exists in the database fails`() =
        suite.runTest {
            val (insertResult, result) = db.query<Person> {
                insert(record = garrison)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            val error = insertResult.error

            assert(error is SurrealError.DB && error.error.isCollision) {
                "The insertion should have failed due to a collision!"
            }
        }

    // GIVEN an empty list of records
    // WHEN insert is called with the list
    // THEN a Failure result is returned with an IllegalArgumentException
    @JsName("queryBuilderTest10")
    @Test
    fun `query builder insert fails with an EmptyArgListException when provided an empty list of records`() =
        suite.runTest {
            val (genres, result) = db.query<List<Genre>> {
                insert<Genre>(records = emptyList())
            }

            assert(result.isFailure) {
                "The query should have failed before attempting to communicate with the database!"
            }

            assert(
                genres is SurrealResult.Failure &&
                genres.error is SurrealError.SDK &&
                (genres.error as SurrealError.SDK).ex is EmptyArgListException
            ) {
                "The insertion should have failed with an EmptyArgListException due to the empty records list!"
            }
        }

    // GIVEN records that do not exist in the database
    // WHEN insert is called on the records
    // THEN all the records are successfully inserted and returned
    @JsName("queryBuilderTest11")
    @Test
    fun `query builder insert successfully inserts multiple records`() =
        suite.runTest {
            val action = Genre(name = "Action")
            val adventure = Genre(name = "Adventure")
            val expected = setOf(action, adventure)

            val (actual, result) = db.query<List<Genre>> {
                insert(records = expected)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "The records should have been inserted!"
            }
        }

    // GIVEN records where all but one exist in the database
    // WHEN insert is called on the records
    // THEN a Failure result is returned with a database error and no records are inserted
    @JsName("queryBuilderTest12")
    @Test
    fun `query builder insert with some records that exist in the database and some that do not will not insert any records`() =
        suite.runTest {
            val action = Genre(name = "Action")

            val (expectedGenres, insert, actualGenres, result) = db.query<List<Genre>, List<Genre>, List<Genre>> {
                keep(
                    getAll(Genre),
                    insert(records = setOf(action, fantasy)),
                    getAll(Genre),
                )
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            val error = insert.error

            assert(
                error != null &&
                error is SurrealError.DB &&
                error.error.isCollision
            ) {
                "The insert should have failed due to a collision!"
            }

            assertEq(
                expected = expectedGenres.value,
                actual = actualGenres.value,
            ) {
                "The insertion should not have changed the database!"
            }
        }

    // GIVEN a record that does not exist in the database
    // WHEN put is called on the record
    // THEN it is successfully inserted and returned
    @JsName("queryBuilderTest13")
    @Test
    fun `query builder put inserts records that do not exist in the database`() =
        suite.runTest {
            val action = Genre(name = "Action")
            val expected = genres.toSet() + action

            val (put, actual, result) = db.query<Genre, List<Genre>> {
                put(record = action) to getAll(table = Genre)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assert(put is SurrealResult.Success && put.value == action) {
                "The put should have returned successfully with the record!"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "The Action genre should have been inserted into the database!"
            }
        }

    // GIVEN a record that exists in the database
    // WHEN put is called on the record
    // THEN the record in the database is successfully updated and returned
    @JsName("queryBuilderTest14")
    @Test
    fun `query builder put updates records that exist in the database`() =
        suite.runTest {
            val expected = spike.copy(firstName = "spike")

            val (put, actual, result) = db.query<Person, Person?> {
                put(record = expected) to get(id = expected)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = put.value,
            ) {
                "The record returned by the database should have been the provided record!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "The record should have been updated in the database!"
            }
        }

    // GIVEN a record that does not exist in the database
    // WHEN update is called on the record
    // THEN the operation no-ops and the database is not altered
    @JsName("queryBuilderTest15")
    @Test
    fun `query builder update no-ops when given a record that does not exist in the database`() =
        suite.runTest {
            val france = Country(name = "France")

            val (update, actual, result) = db.query<Country?, Country?> {
                update(record = france) to get(id = france)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assert(update is SurrealResult.Success && update.value == null) {
                "The update operation should have completed successfully and returned null!"
            }

            assert(actual.value == null) {
                "The record shouldn't have been inserted into the database!"
            }
        }

    // GIVEN a record that exists in the database
    // WHEN update is called on the record
    // THEN the record is successfully updated and returned
    @JsName("queryBuilderTest16")
    @Test
    fun `query builder update updates a record that exists in the database`() =
        suite.runTest {
            val expected = us.copy(name = "USA")

            val (update, actual, result) = db.query<Country?, Country?> {
                update(record = expected) to get(id = expected)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = update.value,
            ) {
                "The record returned by the database should have been the provided record!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "The record should have been updated in the database!"
            }
        }

    // GIVEN a table with records and an update
    // WHEN updateAll is called on the table with the update
    // THEN all the records in the table are updated
    @JsName("queryBuilderTest17")
    @Test
    fun `query builder updateAll replaces all records in the table with the update`() =
        suite.runTest {
            val updatedName = "Adventure"
            val update = Name(name = updatedName)
            val expected = genres.map { it.copy(name = updatedName) }.toSet()

            val (actual, result) = db.query<List<Genre>> {
                updateAll(table = Genre, update = update)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "All the records in the Genre table should have been updated to have the name '$updatedName'!"
            }
        }

    // GIVEN a table with no records and an update
    // WHEN updateAll is called on the table with the update
    // THEN the operation no-ops and the database is not altered
    @JsName("queryBuilderTest18")
    @Test
    fun `query builder updateAll no-ops when called on an empty table`() =
        suite.runTest {
            val update = Company(name = "Google")
            val expected = emptyList<Company>()

            val (updateAll, actual, result) = db.query<List<Company>, List<Company>> {
                updateAll(table = Company, update = update) to getAll(table = Company)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = updateAll.value,
            ) {
                "No records should have been updated!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "The Company table should still be empty!"
            }
        }

    // GIVEN a record that does not exist in the database and an update
    // WHEN merge is called on the record
    // THEN then the merge no-ops and the database is not altered
    @JsName(name = "queryBuilderTest19")
    @Test
    fun `query builder merge creates a record with the provided id and the contents of the update if a record with the id does not exist`() =
        suite.runTest {
            val id = nanoId(Genre)
            val name = "Adventure"
            val update = Name(name = name)
            val expected: Genre? = null

            val (merge, actual, result) = db.query<Genre?, Genre?> {
                merge(id = id, update = update) to get(id = id)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = merge.value,
            ) {
                "The merge should have failed and returned null!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "A record shouldn't have been created in the database!"
            }
        }

    // GIVEN a record that exists in the database and an update
    // WHEN merge is called on the record
    // THEN the record is updated in the database without changes to fields that were not specified in the update
    @JsName(name = "queryBuilderTest20")
    @Test
    fun `query builder merge updates a record in the database with the update without touching fields that were not in the update`() =
        suite.runTest {
            val updatedName = "Garry"
            val update = FirstName(firstName = updatedName)
            val expected = garrison.copy(firstName = updatedName)


            val (merge, actual, result) = db.query<Person?, Person?> {
                merge(id = garrison, update = update) to get(id = garrison)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = merge.value,
            ) {
                "The record returned by the database should have been the provided record with the update applied!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "The merge should have updated the record!"
            }
        }

    // GIVEN a table with records and an update
    // WHEN mergeAll is called on the table with the update
    // THEN all the records in the table are updated without changes to fields that were not specified in the update
    @JsName(name = "queryBuilderTest21")
    @Test
    fun `query builder mergeAll updates records in a table with the update without touching fields that were not in the update`() =
        suite.runTest {
            val updatedName = "name"
            val update = FirstName(firstName = updatedName)
            val expected = people.map { it.copy(firstName = updatedName) }.toSet()

            val (merge, actual, result) = db.query<List<Person>, List<Person>> {
                mergeAll(table = Person, update = update) to getAll(table = Person)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = merge.value?.toSet(),
            ) {
                "The result of the mergeAll should have been a list of Person with the update applied!"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "The merge operation was not properly applied to all records in the table!"
            }
        }

    // GIVEN a table with no records and an update
    // WHEN mergeAll is called on the table with the update
    // THEN the operation no-ops and the database is not altered
    @JsName(name = "queryBuilderTest22")
    @Test
    fun `query builder mergeAll no-ops when called on a table with no records`() =
        suite.runTest {
            val update = Name(name = "Google")
            val expected = emptyList<Company>()

            val (merge, actual, result) = db.query<List<Company>, List<Company>> {
                mergeAll(table = Company, update = update) to getAll(table = Company)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = merge.value,
            ) {
                "The mergeAll should have no-oped and returned an empty list!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "The table should not have had any records inserted into it!"
            }
        }

    // TODO(garrison): create tests that compare update/updateAll and merge/mergeAll e.g. have an optional field that has a value and
    // TODO: show that an update without the field will cause it to be null while the merge will leave its current value

    // GIVEN a record id that exists in the database
    // WHEN delete is called on that id
    // THEN the record is removed from the database
    @JsName(name = "queryBuilderTest23")
    @Test
    fun `query builder delete removes an existing record with a matching id from the database`() =
        suite.runTest {
            val recordToDelete = fantasy
            val expected = genres.toSet() - recordToDelete

            val (delete, actual, result) = db.query<Genre?, List<Genre>> {
                delete(id = recordToDelete) to getAll(table = Genre)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = recordToDelete,
                actual = delete.value,
            ) {
                "The deleted record should have been returned!"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "The table should not longer contain the fantasy genre!"
            }
        }

    // GIVEN a record id that does not exist in the database
    // WHEN delete is called on that id
    // THEN the operation no-ops and the database is not altered
    @JsName(name = "queryBuilderTest24")
    @Test
    fun `query builder delete no-ops when called with an id that does not exist in the database`() =
        suite.runTest {
            val recordToDelete = Genre(name = "Action")
            val expected = genres.toSet()

            val (delete, actual, result) = db.query<Genre?, List<Genre>> {
                delete(id = recordToDelete) to getAll(table = Genre)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = null,
                actual = delete.value,
            ) {
                "No record should have been deleted!"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "Nothing should have been deleted from the table!"
            }
        }

    // GIVEN record ids that exist in the database
    // WHEN delete is called with the id
    // THEN all the records are removed from the database
    @JsName(name = "queryBuilderTest25")
    @Test
    fun `query builder delete removes existing records with matching ids from the database`() =
        suite.runTest {
            val recordsToDelete = setOf(fantasy, drama)
            val expected = genres.toSet() - recordsToDelete


            val (delete, actual, result) = db.query<List<Genre>, List<Genre>> {
                delete(ids = recordsToDelete) to getAll(table = Genre)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = recordsToDelete,
                actual = delete.value?.toSet(),
            ) {
                "delete should have returned the two records that were supposed to be deleted!"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "The table contains records that should have been deleted!"
            }
        }

    // GIVEN several record ids where one does not exist in the database
    // WHEN delete is called with the ids
    // THEN all the records with matching ids are removed from the database
    @JsName(name = "queryBuilderTest26")
    @Test
    fun `query builder delete removes existing records with matching ids from the database and ignores ids that do not exist in the database`() =
        suite.runTest {
            val action = Genre(name = "Action")
            val expectedDeletedRecords = setOf(fantasy, drama)
            val recordsToDelete = expectedDeletedRecords + action
            val expected = genres.toSet() - recordsToDelete

            val (delete, actual, result) = db.query<List<Genre>, List<Genre>> {
                delete(ids = recordsToDelete) to getAll(table = Genre)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expectedDeletedRecords,
                actual = delete.value?.toSet(),
            ) {
                "delete should have returned the two records that were supposed to be deleted!"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "The table contains records that should have been deleted!"
            }
        }

    // GIVEN record ids that do not exist in the database
    // WHEN delete is called with the ids
    // THEN the operation no-ops and the database is not altered
    @JsName(name = "queryBuilderTest27")
    @Test
    fun `query builder delete no-ops if none of the provided ids exist in the database`() =
        suite.runTest {
            val action = Genre(name = "Action")
            val adventure = Genre(name = "Adventure")
            val expectedDeletedRecords = emptyList<Genre>()
            val recordsToDelete = listOf(action, adventure)
            val expected = genres.toSet()

            val (delete, actual, result) = db.query<List<Genre>, List<Genre>> {
                delete(ids = recordsToDelete) to getAll(table = Genre)
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = expectedDeletedRecords,
                actual = delete.value,
            ) {
                "delete should have returned no records because none of the input records exist in the database!"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "No records should have been deleted from the database!"
            }
        }

    // GIVEN a table with records
    // WHEN deleteAll is called on the table
    // THEN all the table's records are removed from the database
    @JsName(name = "queryBuilderTest28")
    @Test
    fun `query builder deleteAll deletes all records in a table`() =
        suite.runTest {
            val expected = emptyList<Genre>()
            val expectedDeletedRecords = genres.toSet()

            val (delete, actual, result) = db.query<List<Genre>, List<Genre>> {
                deleteAll(table = Genre) to getAll(table = Genre)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expectedDeletedRecords,
                actual = delete.value?.toSet(),
            ) {
                "deleteAll should have returned all the records in the database!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "All of the records in the genre table should have been deleted!"
            }
        }

    // GIVEN a table with no records
    // WHEN deleteAll is called on the table
    // THEN the operation no-ops and the database is not altered
    @JsName(name = "queryBuilderTest29")
    @Test
    fun `query builder deleteAll no-ops when called on an empty table`() =
        suite.runTest {
            val expected = emptyList<Company>()

            val (delete, actual, result) = db.query<List<Company>, List<Company>> {
                deleteAll(table = Company) to getAll(table = Company)
            }

            assert(result.isSuccess) {
                "The query should have succeeded"
            }

            assertEq(
                expected = expected,
                actual = delete.value,
            ) {
                "No records should have been deleted!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "The table should still be empty!"
            }
        }

    // GIVEN a query that selects no records
    // WHEN queryOne is called with the query
    // THEN a Failure result is returned containing a NonSingleRecordResultException
    @JsName(name = "queryBuilderTest30")
    @Test
    fun `query builder queryOne fails when the query returns no records`() =
        suite.runTest {
            val query = "select * from company"

            val (select, result) = db.query<Company> {
                queryOne(query = query)
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assert(select.error is SurrealError.SDK) {
                "queryOne should fail when no records are found!"
            }

            assert((select.error as? SurrealError.SDK)?.ex is NonSingleRecordResultException) {
                "queryOne should return a NonSingleRecordResultException when the result of its query contains no records!"
            }
        }

    // GIVEN a query that selects all the fields of a single record
    // WHEN queryOne is called with the query
    // THEN the record is returned
    @JsName(name = "queryBuilderTest31")
    @Test
    fun `query builder queryOne succeeds when the query returns a single record`() =
        suite.runTest {
            val query = "select * from genre where id = \$id"
            val expected = fantasy

            val (actual, result) = db.query<Genre> {
                queryOne(query = query, parameters = mapOf("id" to expected.id))
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "queryOne should successfully return the record of a query that results in a single record!"
            }
        }

    // GIVEN a query that selects all the fields of multiple records
    // WHEN queryOne is called with the query
    // THEN a Failure result is returned containing a NonSingleRecordResultException
    @JsName(name = "queryBuilderTest32")
    @Test
    fun `query builder queryOne fails when the query returns more than one record`() =
        suite.runTest {
            val query = "select * from person"

            val (select, result) = db.query<Person> {
                queryOne(query = query)
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assert(select.error is SurrealError.SDK) {
                "queryOne should fail when more than one record is found!"
            }

            assert((select.error as? SurrealError.SDK)?.ex is NonSingleRecordResultException) {
                "queryOne should return a NonSingleRecordResultException when the result of its query contains more than one record!"
            }
        }

    // GIVEN a query that projects the fields of a single record
    // WHEN queryOne is called with the query and a container for the projected record
    // THEN the record is projected into the container
    @JsName(name = "queryBuilderTest33")
    @Test
    fun `query builder queryOne can project its result`() =
        suite.runTest {
            val id = garrison.idString
            val query1 = "select firstName from person where id = \$id"
            val query2 = "select * from person where id = \$id"
            val expected = FirstName(firstName = garrison.firstName)

            val (select1, select2, result) = db.query<FirstName, FirstName> {
                queryOne<FirstName>(query = query1, parameters = mapOf("id" to id)) to
                    queryOne<FirstName>(query = query2, parameters = mapOf("id" to id))
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = expected,
                actual = select1.value,
            ) {
                "queryOne should be able to collect the projected fields into the specified container!"
            }

            assertEq(
                expected = expected,
                actual = select2.value,
            ) {
                "queryOne should be able to project the result fields into the specified container!"
            }
        }

    // GIVEN a query containing multiple statements
    // WHEN queryOne is called with the query
    // THEN a Failure result is returned containing an QueryResultCountMismatchException
    @JsName(name = "queryBuilderTest34")
    @Test
    fun `query builder queryOne fails when given more than one statement in its query`() =
        suite.runTest {
            val query = "select * from person; delete person;"

            val (select, result) = db.query<Person> {
                queryOne(query = query)
            }

            assert(result.isFailure) {
                "The query should have failed!"
            }

            assert(select.error is SurrealError.SDK) {
                "queryOne should fail when more than one statement is included in the query!"
            }

            assert((select.error as? SurrealError.SDK)?.ex is QueryResultCountMismatchException) {
                "queryOne should return a QueryResultCountMismatchException when its query contains more than one statement!"
            }
        }

    // GIVEN a query that selects no records
    // WHEN queryMany is called with the query
    // THEN an empty list is returned
    @JsName(name = "queryBuilderTest35")
    @Test
    fun `query builder queryMany returns an empty list when its query selects no records`() =
        suite.runTest {
            val expected = emptyList<Genre>()
            val query = "select * from genre where id = \$id"

            val (actual, result) = db.query<List<Genre>> {
                queryMany(query = query, parameters = mapOf("id" to "42"))
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "The query should have returned no results!"
            }
        }

    // GIVEN a query that selects all the fields of a single record
    // WHEN queryMany is called with the query
    // THEN a list containing the single record is returned
    @JsName(name = "queryBuilderTest36")
    @Test
    fun `query builder queryMany returns a list containing a single record when its query selects a single record`() =
        suite.runTest {
            val expected = listOf(sciFi)
            val query = "select * from genre where id = \$id"

            val (actual, result) = db.query<List<Genre>> {
                queryMany(query = query, parameters = mapOf("id" to sciFi))
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "The query should have returned a list containing a single Sci-Fi genre!"
            }
        }

    // GIVEN a query that selects all the fields of multiple records
    // WHEN queryMany is called with the query
    // THEN a list containing the records is returned
    @JsName(name = "queryBuilderTest37")
    @Test
    fun `query builder queryMany returns a list of multiple records when its query selects more than one record`() =
        suite.runTest {
            val expected = setOf(sciFi, comedy)
            val query = "select * from genre where id in \$ids"

            val (actual, result) = db.query<List<Genre>> {
                queryMany(query = query, parameters = mapOf("ids" to expected))
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = expected,
                actual = actual.value?.toSet(),
            ) {
                "The query should have returned a list containing the Sci-Fi and Comedy genre!"
            }
        }

    // GIVEN a query that projects the fields of several records
    // WHEN queryMany is called with the query and a container for the projected records
    // THEN the record is projected into a list of projection containers
    @JsName(name = "queryBuilderTest38")
    @Test
    fun `query builder queryMany can project its results`() =
        suite.runTest {
            val query1 = "select firstName from person"
            val query2 = "select * from person"
            val expected = people.map { FirstName(firstName = it.firstName) }.toSet()

            val (select1, select2, result) = db.query<List<FirstName>, List<FirstName>> {
                queryMany<FirstName>(query = query1) to queryMany<FirstName>(query = query2)
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = expected,
                actual = select1.value?.toSet(),
            ) {
                "queryMany should be able to collect the projected fields into the specified containers!"
            }

            assertEq(
                expected = expected,
                actual = select2.value?.toSet(),
            ) {
                "queryMany should be able to project the result fields into the specified containers!"
            }
        }

    // GIVEN a query containing multiple statements
    // WHEN queryMany is called with the query
    // THEN a Failure result is returned containing a QueryResultCountMismatchException
    @JsName(name = "queryBuilderTest39")
    @Test
    fun `query builder queryMany fails when given more than one statement in its query`() =
        suite.runTest {
            val query = "select * from person; delete person;"

            val (select, result) = db.query<List<Person>> {
                queryMany<Person>(query = query)
            }

            assert(result.isFailure) {
                "The query should have failed!"
            }

            assert(select.error is SurrealError.SDK) {
                "queryMany should fail when more than one statement is included in the query!"
            }

            assert((select.error as SurrealError.SDK).ex is QueryResultCountMismatchException) {
                "queryMany should return a QueryResultCountMismatchException when its query contains more than one statement!"
            }
        }

    // GIVEN a blank query
    // WHEN statement is called with the query
    // THEN a Failure result is returned containing a QueryResultCountMismatchException
    @JsName(name = "queryBuilderTest40")
    @Test
    fun `query builder statement fails when given a blank query`() =
        suite.runTest {
            val (statementResult, queryResult) = db.query<RawSurrealStatementResult> {
                +""
            }

            assert(queryResult.isFailure) {
                "The query should have failed!"
            }

            assert(statementResult.error is SurrealError.SDK) {
                "statement should fail when no query is provided!"
            }

            assert((statementResult.error as SurrealError.SDK).ex is QueryResultCountMismatchException) {
                "statement should return an QueryResultCountMismatchException when its query contains no statements!"
            }
        }

    // GIVEN a query with a single statement
    // WHEN statement is called with the query
    // THEN a list of results for the statement is returned
    @JsName(name = "queryBuilderTest41")
    @Test
    fun `query builder statement returns a list containing the statement results when given a single statement`() =
        suite.runTest {
            val expected = listOf(nullSerializer.encodeToJsonElement(manga))

            val (actual, result) = db.query<RawSurrealStatementResult> {
                "select * from medium where id = \$id" params mapOf("id" to manga.id)
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = expected,
                actual = actual.value,
            ) {
                "statement should return a list of JsonElement with the results of the statement!"
            }
        }

    // GIVEN a query with multiple statements
    // WHEN statement is called with the query
    // THEN a Failure result is returned containing a QueryResultCountMismatchException
    @JsName(name = "queryBuilderTest42")
    @Test
    fun `query builder statement fails when given a query with a multiple statement`() =
        suite.runTest {
            val (select, result) = db.query<RawSurrealStatementResult> {
                "select * from medium where id = \$id; select * from genre" params mapOf("id" to manga.id)
            }

            assert(result.isFailure) {
                "The query should have failed!"
            }

            assert(select.error is SurrealError.SDK) {
                "The select should fail when more than one statement is included in the query!"
            }

            assert((select.error as SurrealError.SDK).ex is QueryResultCountMismatchException) {
                "statement should return a QueryResultCountMismatchException for each result when its query contains more than one statement!"
            }
        }

    @JsName(name = "queryBuilderTest43")
    @Test
    fun `query builder relate creates an edge between two records`() =
        suite.runTest {
            val query = "select * from person where ->reads->(medium where name = 'Light Novel')"
            val expectedPerson = garrison
            val expectedMedium = lightNovel

            val (actualEdge, actualPerson, result) = db.query<Reads, Person> {
                relate(table = Reads, `in` = expectedPerson, out = expectedMedium) to
                    queryOne<Person>(query = query)
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assert(actualEdge.value?.idString?.startsWith(prefix = Reads.tableName) == true) {
                "The id of the relation should start with the name of the edge table!"
            }

            assertEq(
                expected = expectedPerson.id,
                actual = actualEdge.value?.`in`,
            ) {
                "The in record of the relation should be the garrison Person record!"
            }

            assertEq(
                expected = expectedMedium.id,
                actual = actualEdge.value?.out,
            ) {
                "The out record of the relation should be the lightNovel Medium record!"
            }

            assertEq(
                expected = expectedPerson,
                actual = actualPerson.value,
            ) {
                "The Person garrison should have been found using the new relation!"
            }
        }

    @JsName(name = "queryBuilderTest44")
    @Test
    fun `query builder relate can create edges between two records that contain additional data`() =
        suite.runTest {
            val query = "select * from person where ->livesIn->(country where name = 'United States of America')"
            val expectedPerson = garrison
            val expectedCountry = us
            val expectedYears = 25
            val edge = LivesIn(
                `in` = expectedPerson.id,
                out = expectedCountry.id,
                durationInYears = expectedYears,
            )

            val (actualEdge, actualPerson, result) = db.query<LivesIn, Person> {
                relate(edge = edge) to queryOne<Person>(query = query)
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assert(actualEdge.value?.idString?.startsWith(prefix = LivesIn.tableName) == true) {
                "The id of the relation should start with the name of the edge table!"
            }

            assertEq(
                expected = expectedPerson.id,
                actual = actualEdge.value?.`in`,
            ) {
                "The in record of the relation should be the garrison Person record!"
            }

            assertEq(
                expected = expectedCountry.id,
                actual = actualEdge.value?.out,
            ) {
                "The out record of the relation should be the us Country record!"
            }

            assertEq(
                expected = expectedYears,
                actual = actualEdge.value?.durationInYears,
            ) {
                "The out record of the relation should be the us Country record!"
            }

            assertEq(
                expected = expectedPerson,
                actual = actualPerson.value,
            ) {
                "The Person garrison should have been found using the new relation!"
            }
        }

    @JsName(name = "queryBuilderTest45")
    @Test
    fun `query can execute multiple statements`() =
        suite.runTest {
            val expectedPerson = garrison

            val (personResult, result) = db.query<Person> {
                val book = Medium(name = "Book")
                insert(record = book)

                val edge = Reads(`in` = garrison.id, out = book.id)
                relate(edge = edge)

                queryOne(query = "select * from person where ->reads->(medium where name = 'Book')")
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = expectedPerson,
                actual = personResult.value,
            ) {
                "The insertion, relation, or query failed!"
            }
        }

    // GIVEN a query with multiple statements
    // WHEN the statements are executed in a transaction
    // THEN the query and all its statements return successfully
    @JsName(name = "queryBuilderTest46")
    @Test
    fun `query transactions complete successfully when no errors occur`() =
        suite.runTest {
            val france = Country(name = "French Republic")
            val uk = Country(name = "United Kingdom of Great Britain and Northern Ireland")
            val expectedCountries1 = countries.toSet() + france
            val expectedCountries2 = expectedCountries1 + uk

            val (insert1, get1, insert2, get2, result) = db.query<Country, List<Country>, Country, List<Country>> {
                beginTransaction()
                val insert1 = insert(record = france)
                val get1 = getAll(table = Country)
                val insert2 = insert(record = uk)
                commitTransaction()
                val get2 = getAll(table = Country)
                keep(insert1, get1, insert2, get2)
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assertEq(
                expected = france,
                actual = insert1.value,
            ) {
                "The first insert should have added France to the database!"
            }

            assertEq(
                expected = expectedCountries1,
                actual = get1.value?.toSet(),
            ) {
                "The first get should have returned all the original countries plus France!"
            }

            assertEq(
                expected = uk,
                actual = insert2.value,
            ) {
                "The second insert should have added the UK to the database!"
            }

            assertEq(
                expected = expectedCountries2,
                actual = get2.value?.toSet(),
            ) {
                "The first get should have returned all the original countries plus France and the UK!"
            }
        }

    // GIVEN a query with multiple statements
    // WHEN the statements are executed in a transaction and one statement fails
    // THEN the successful statements prior to the failure are rolled back and the cause of the failure is returned
    @JsName(name = "queryBuilderTest47")
    @Test
    fun `query transactions rollback all successfully completed statements if an error occurs`() =
        suite.runTest {
            val expectedCountries = countries.toSet()
            val france = Country(name = "French Republic")

            val (insert1, get1, insert2, get2, result) = db.query<Country, List<Country>, Country, List<Country>> {
                beginTransaction()
                val insert1 = insert(record = france)
                val get1 = getAll(table = Country)
                val insert2 = insert(record = us)
                commitTransaction()
                val get2 = getAll(table = Country)
                keep(insert1, get1, insert2, get2)
            }

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            assert(
                insert1.error is SurrealError.DB &&
                get1.error is SurrealError.DB &&
                insert2.error is SurrealError.DB
            ) {
                "All the statements in the transaction should have failed!"
            }

            assert(
                (insert1.error as SurrealError.DB).error.isRolledBack &&
                (get1.error as SurrealError.DB).error.isRolledBack
            ) {
                "All the statements in the transaction prior to the error should have been rolled back!"
            }

            assert((insert2.error as SurrealError.DB).error.isCollision) {
                "The insertion should have failed because the record already exists in the database!"
            }

            assertEq(
                expected = expectedCountries,
                actual = get2.value?.toSet(),
            ) {
                "The transaction should have rolled back any changes to the countries table!"
            }
        }

    @JsName("queryBuilderTest48")
    @Test
    fun `query builder live starts a live query on the specified table that can be later stopped with kill`() =
        suite.runTest {
            val uk = Country(name = "United Kingdom of Great Britain and Northern Ireland")
            val updatedUK = uk.copy(name = "United Kingdom")
            val france = Country(name = "French Republic")
            val expectedUpdates = listOf(
                SurrealLiveQueryUpdate.Create(record = uk),
                SurrealLiveQueryUpdate.Update(record = updatedUK),
                SurrealLiveQueryUpdate.Delete(id = updatedUK.id),
            )

            val (liveResponseResult, result) = db.query<SurrealLiveQueryResponse<Country>> {
                live(table = Country)
            }
            val liveResponse = liveResponseResult.value

            assert(result.isSuccess) {
                "The query should have succeeded!"
            }

            printlnToStdErr(msg = liveResponseResult)

            assert(liveResponse != null) { "The live query start request should have succeeded!" }

            val updates = liveResponse.updates.consumeAsFlow().shareIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                replay = 4,
            )

            val (changes) = db.transaction {
                insert(record = uk)
                update(record = updatedUK)
                delete(id = updatedUK)
                NONE
            }

            assert(changes.isSuccess) { "The transaction with the changes should have succeeded!" }

            val (killResult) = db.query<Unit> {
                kill(handle = liveResponse.handle)
            }

            assert(killResult.isSuccess) { "The kill should have succeeded!" }

            val (ignoredInsert) = db.query<Country> {
                insert(record = france)
            }

            assertEq(
                expected = france,
                actual = ignoredInsert.value,
            ) {
                "The record returned by the database insertion should match the provided record!"
            }

            advanceUntilIdle()

            val actualUpdates = withContext(Dispatchers.IO) {
                delay(timeMillis = 500)
                advanceUntilIdle()
                updates.replayCache
            }

            assertEq(
                expected = expectedUpdates,
                actual = actualUpdates,
            ) {
                "The insertion, update, and deletion should have been received as live updates and " +
                    "the final insertion should have been ignored!"
            }
        }
}
