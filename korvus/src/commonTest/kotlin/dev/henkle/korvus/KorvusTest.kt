package dev.henkle.korvus

import dev.henkle.korvus.error.KorvusError
import dev.henkle.korvus.ext.delete
import dev.henkle.korvus.ext.deleteByIDPrefix
import dev.henkle.korvus.ext.get
import dev.henkle.korvus.ext.getAll
import dev.henkle.korvus.ext.put
import dev.henkle.korvus.ext.queryMany
import dev.henkle.korvus.ext.queryOne
import dev.henkle.korvus.ext.typedBatch
import dev.henkle.korvus.utils.Person
import dev.henkle.korvus.utils.Series
import dev.henkle.korvus.utils.TestData.countryTempest
import dev.henkle.korvus.utils.TestData.countryUS
import dev.henkle.korvus.utils.TestData.genreSliceOfLife
import dev.henkle.korvus.utils.TestData.person1
import dev.henkle.korvus.utils.TestData.person10
import dev.henkle.korvus.utils.TestData.person15
import dev.henkle.korvus.utils.TestData.person2
import dev.henkle.korvus.utils.TestData.person20
import dev.henkle.korvus.utils.TestData.person21
import dev.henkle.korvus.utils.TestData.person3
import dev.henkle.korvus.utils.TestData.person4
import dev.henkle.korvus.utils.TestData.person5
import dev.henkle.korvus.utils.TestData.series1
import dev.henkle.korvus.utils.TestData.series2
import dev.henkle.korvus.utils.TestData.series3
import dev.henkle.korvus.utils.TestData.series4
import dev.henkle.korvus.utils.TestData.series5
import dev.henkle.korvus.utils.TestData.series6
import dev.henkle.korvus.utils.assertPresentInDB
import dev.henkle.korvus.utils.cleanForComparison
import dev.henkle.korvus.utils.id
import dev.henkle.test.assert
import dev.henkle.test.assertEq
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsName
import kotlin.reflect.typeOf
import kotlin.test.BeforeTest
import kotlin.test.Test

class KorvusTest {
    companion object {
        lateinit var db: KorvusDatabase
        init {
            runTest {
                db = Korvus.create("http://localhost:8080").db.get(name = "Test").result!!
            }
        }
    }

    @BeforeTest
    fun wipeDB() = runTest {
        db.deleteByQuery("from @all_docs")
    }

    @JsName("test1")
    @Test
    fun `Put with a single document`() = runTest {
        val expected = person1
        db.put(expected)
        val person = db.get<Person>(id = expected.id).result
        person.assertPresentInDB()
        assertEq(expected = expected, actual = person.cleanForComparison()) {
            "person1 was not properly inserted and retrieved from the database!"
        }
    }

    @JsName("test2")
    @Test
    fun `Put with multiple documents`() = runTest {
        val expected = listOf(person2, person3, person4).toSet()
        db.put(documents = expected)
        val people = db.getAll(Person).result
        people.assertPresentInDB()
        assertEq(
            expected = expected,
            actual = people.cleanForComparison().toSet(),
        ) {
            "persons 1-4 should have been inserted and retrieved from the database!"
        }
    }

    @JsName("test3")
    @Test
    fun `Put with an empty string ID auto-generates the ID`() = runTest {
        val documentWithoutId = Person(firstName = "", lastName = "", country = id(""), series = id(""))
        db.put(documentWithoutId)
        val person = db.getAll(Person).result
        person.assertPresentInDB()
        val id = person.firstOrNull()?.id
        assert(id != null && id != "")
    }

    @JsName("test4")
    @Test
    fun `Delete by ID`() = runTest {
        val expectedBefore = setOf(person1, person2, person3)
        val expectedAfter = setOf(person1, person3)
        db.put(documents = expectedBefore)

        val peopleBeforeDeletion = db.getAll(Person).result
        assertEq(
            expected = expectedBefore,
            actual = peopleBeforeDeletion?.cleanForComparison(),
        ) {
            "3 people should have been inserted into the database!"
        }

        val deletionResult = db.delete<Person>(id = person2.id)
        assert(deletionResult is KorvusResult.Success) {
            "Deletion operation should have succeeded!"
        }

        val peopleAfterDeletion = db.getAll(Person).result
        assertEq(
            expected = expectedAfter,
            actual = peopleAfterDeletion?.cleanForComparison(),
        ) {
            "person2 was not deleted from the database!"
        }
    }

    @JsName("test5")
    @Test
    fun `Delete by ID prefix`() = runTest {
        val expectedBefore = setOf(person1, person2, person3, person15, person20, person21)
        val expectedAfter = setOf(person1, person3, person15)
        db.put(documents = expectedBefore)

        val peopleBeforeDeletion = db.getAll(Person).result
        assertEq(
            expected = expectedBefore,
            actual = peopleBeforeDeletion?.cleanForComparison(),
        ) {
            "6 people should have been inserted into the database!"
        }

        val deletionResult = db.deleteByIDPrefix<Person>(prefix = "2")
        assert(deletionResult is KorvusResult.Success) {
            "Deletion operation should have succeeded!"
        }

        val peopleAfterDeletion = db.getAll(Person).result
        assertEq(
            expected = expectedAfter,
            actual = peopleAfterDeletion?.cleanForComparison(),
        ) {
            "person2, person20, and person21 should have been deleted from the database!"
        }
    }

    @JsName("test6")
    @Test
    fun `Delete by ID not present in the DB no-ops and returns successfully`() = runTest {
        val expected = setOf(person1, person3, person15, person20)

        db.put(documents = expected)

        val peopleBeforeDeletion = db.getAll(Person).result
        assertEq(
            expected = expected,
            actual = peopleBeforeDeletion?.cleanForComparison(),
        ) {
            "4 people should have been inserted into the database!"
        }

        db.delete<Person>(id = "2")

        val peopleAfterDeletion = db.getAll(Person).result
        assertEq(
            expected = expected,
            actual = peopleAfterDeletion?.cleanForComparison(),
        ) {
            "The deletion should not have deleted anything from the database!"
        }
    }

    @JsName("test7")
    @Test
    fun `queryOne with a query that returns a single document`() = runTest {
        val documents = listOf(person1, person2, person3)
        val expected = person1

        db.put(documents = documents)

        val actual = db.queryOne<Person>(
            query = "from Person where id() = \$id",
            parameters = mapOf("id" to expected.id),
        ).result

        assertEq(
            expected = expected,
            actual = actual?.cleanForComparison(),
        ) {
            "person1 should have been retrieved from the database!"
        }
    }

    @JsName("test8")
    @Test
    fun `queryOne with a query that returns multiple documents results in a IllegalStateException`() = runTest {
        val documents = listOf(person1, person2, person3)

        db.put(documents = documents)

        val result = db.queryOne<Person>(query = "from Person select *").error

        assert(result is KorvusError.SDK && result.ex is IllegalStateException) {
            "queryOne should throw an IllegalStateException when multiple documents are returned!"
        }
    }

    @JsName("test9")
    @Test
    fun `queryOne with a query that returns no documents results in a IllegalStateException`() = runTest {
        val result = db.queryOne<Person>(query = "from Person select *").error

        assert(result is KorvusError.SDK && result.ex is IllegalStateException) {
            "queryOne should throw an IllegalStateException when multiple documents are returned!"
        }
    }

    @JsName("test10")
    @Test
    fun `Projection with queryOne`() = runTest {
        val document = person1

        db.put(document = document)

        val name = db.queryOne<Name>(
            query = "from Person as p where id(p) = \$id select p.firstName, p.lastName as LNAME",
            parameters = mapOf("id" to document.id),
        ).result

        assertEq(
            expected = document.firstName,
            actual = name?.firstName,
        ) {
            "The projection was expected to have the same firstName as the Person in the database!"
        }

        assertEq(
            expected = document.lastName,
            actual = name?.lastName,
        ) {
            "The projection was expected to have the same lastName as the Person in the database!"
        }
    }

    @JsName("test11")
    @Test
    fun `queryMany with a query that returns no documents`() = runTest {
        val expectedResults = emptyList<Person>()
        val expectedResultCount = 0
        val result = db.queryMany<Person>(query = "from Person select *").result

        assertEq(
            expected = expectedResultCount,
            actual = result?.results?.size,
        ) {
            "Querying an empty database should result in zero results!"
        }

        assertEq(
            expected = expectedResults,
            actual = result?.results,
        ) {
            "Querying an empty database should result in no matching documents!"
        }
    }

    @JsName("test12")
    @Test
    fun `queryMany with a query that returns a single documents`() = runTest {
        val documents = listOf(person1, person2)

        db.put(documents = documents)

        val expectedResults = setOf(person2)
        val expectedResultCount = 1
        val result = db.queryMany<Person>(
            query = "from Person where id() = \$id select *",
            parameters = mapOf("id" to person2.id),
        ).result

        assertEq(
            expected = expectedResultCount,
            actual = result?.results?.size,
        ) {
            "The query should result in a single result!"
        }

        assertEq(
            expected = expectedResults,
            actual = result?.results?.cleanForComparison(),
        ) {
            "The query should result in a single matching document!"
        }
    }

    @JsName("test13")
    @Test
    fun `queryMany with a query that returns multiple documents`() = runTest {
        val documents = listOf(person1, person2, person3, person15, person20, person21)

        db.put(documents = documents)

        val expectedResults = setOf(person2, person20, person21)
        val expectedResultCount = 3
        val result = db.queryMany<Person>(
            query = "from Person as p where startsWith(id(p), \$prefix) select *",
            parameters = mapOf("prefix" to "2"),
        ).result

        assertEq(
            expected = expectedResultCount,
            actual = result?.results?.size,
        ) {
            "The query should result in 3 results!"
        }

        assertEq(
            expected = expectedResults,
            actual = result?.results?.cleanForComparison(),
        ) {
            "The query should result in a 3 matching documents!"
        }
    }

    @JsName("test14")
    @Test
    fun `Projection with queryMany`() = runTest {
        val documents = setOf(person1, person2)
        val expectedResultCount = 2
        val expectedNames = documents
            .map { Name(firstName = it.firstName, lastName = it.lastName) }
            .toSet()

        db.put(documents = documents)

        val result = db.queryMany<Name>(
            query = "from Person as p select p.firstName, p.lastName as LNAME",
        ).result

        assertEq(
            expected = expectedResultCount,
            actual = result?.results?.size,
        ) {
            "The query should result in 2 results!"
        }

        assertEq(
            expected = expectedNames,
            actual = result?.results?.toSet(),
        ) {
            "The query should result in a 2 matching documents!"
        }
    }

    @JsName("test15")
    @Test
    fun `Batch put`() = runTest {
        val documents = setOf(person1, person2)
        val document = person3
        val expectedDocuments = documents + document

        val batchSuccessful = db.batch {
            put(documents = documents)
            put(document = document)
        }.isSuccess

        assert(batchSuccessful) { "The batch put should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Batch put should have inserted 3 documents into the database!"
        }
    }

    @JsName("test16")
    @Test
    fun `Batch delete`() = runTest {
        val documents = setOf(person1, person2, person3, person4, person5, person10, person15)
        val expectedDocuments = setOf(person4)

        val putSuccessful = db.put(documents = documents).isSuccess

        assert(putSuccessful) { "The setup put should have been successful!" }

        val insertedDocumentCount = db.getAll(Person).result?.size

        assertEq(
            expected = documents.size,
            actual = insertedDocumentCount,
        ) {
            "Some of the setup documents were not properly inserted into the database!"
        }

        val batchSuccessful = db.batch {
            delete(document = person1)
            delete(documents = listOf(person2, person3))
            delete<Person>(id = person5.id)
            delete<Person>(ids = listOf(person10.id, person15.id))
        }.isSuccess

        assert(batchSuccessful) { "The batch delete should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Batch delete should have deleted all but one document from the database!"
        }
    }

    @JsName("test17")
    @Test
    fun `Batch delete by ID`() = runTest {
        val documents = setOf(person1, person2, person10, person15)
        val expectedDocuments = setOf(person2)

        val putSuccessful = db.put(documents = documents).isSuccess

        assert(putSuccessful) { "The setup put should have been successful!" }

        val insertedDocumentCount = db.getAll(Person).result?.size

        assertEq(
            expected = documents.size,
            actual = insertedDocumentCount,
        ) {
            "Some of the setup documents were not properly inserted into the database!"
        }

        val batchSuccessful = db.batch {
            deleteByIDPrefix<Person>(prefix = "1")
        }.isSuccess

        assert(batchSuccessful) { "The batch delete by id should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Batch delete by id should have deleted all but one document from the database!"
        }
    }

    @JsName("test18")
    @Test
    fun `Put and delete in same batch`() = runTest {
        val documents = setOf(person1, person2, person3)
        val expectedDocuments = setOf(person1, person3)

        val batchSuccessful = db.batch {
            put(documents = documents)
            delete(document = person2)
        }.isSuccess

        assert(batchSuccessful) { "The batch should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "All the documents but person2 should have been inserted into the database!"
        }
    }

    @JsName("test19")
    @Test
    fun `Put and delete by ID in same batch`() = runTest {
        val documents = setOf(person1, person2, person10, person15)
        val expectedDocuments = setOf(person2)

        val batchSuccessful = db.batch {
            put(documents = documents)
            deleteByIDPrefix<Person>(prefix = "1")
        }.isSuccess

        assert(batchSuccessful) { "The batch should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Only person2 should have been inserted into the database!"
        }
    }

    @JsName("test20")
    @Test
    fun `Put delete and delete by ID in same batch`() = runTest {
        val documents = setOf(person1, person2, person3, person10, person15)
        val expectedDocuments = setOf(person2)

        val batchSuccessful = db.batch {
            put(documents = documents)
            deleteByIDPrefix<Person>(prefix = "1")
            delete(document = person3)
        }.isSuccess

        assert(batchSuccessful) { "The batch should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Only person2 should have been inserted into the database!"
        }
    }

    @JsName("test21")
    @Test
    fun `Use different types in the same batch`() = runTest {
        val people = setOf(person1, person2, person3, person4, person10)
        val aPerson = person5
        val series = setOf(series1, series2, series3, series4, series6)
        val aSeries = series5
        val expectedPeople = setOf(person3, person5)
        val expectedSeries = setOf(series3, series5)

        val batchSuccessful = db.batch {
            put(documents = people)
            put(documents = series)
            put(document = aPerson)
            put(document = aSeries)
            delete(document = person1)
            delete(document = series1)
            deleteByIDPrefix<Person>(prefix = "2")
            deleteByIDPrefix<Series>(prefix = series2.id)
            delete(documents = listOf(person4, person10))
            delete(documents = listOf(series4, series6))
        }.isSuccess

        assert(batchSuccessful) { "The batch should have succeeded!" }

        val actualPeople = db.getAll(Person).result?.cleanForComparison()
        val actualSeries = db.getAll(Series).result?.cleanForComparison()

        assertEq(
            expected = expectedPeople,
            actual = actualPeople,
        ) {
            "Some of the operations failed for the Person type!"
        }

        assertEq(
            expected = expectedSeries,
            actual = actualSeries,
        ) {
            "Some of the operations failed for the Series type!"
        }
    }

    @JsName("test22")
    @Test
    fun `Typed batch put`() = runTest {
        val documents = setOf(person1, person2)
        val document = person3
        val expectedDocuments = documents + document

        val batchSuccessful = db.typedBatch {
            put(documents = documents)
            put(document = document)
        }.isSuccess

        assert(batchSuccessful) { "The batch put should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Batch put should have inserted 3 documents into the database!"
        }
    }

    @JsName("test23")
    @Test
    fun `Typed batch delete`() = runTest {
        val documents = setOf(person1, person2, person3, person4, person5, person10, person15)
        val expectedDocuments = setOf(person4)

        val putSuccessful = db.put(documents = documents).isSuccess

        assert(putSuccessful) { "The setup put should have been successful!" }

        val insertedDocumentCount = db.getAll(Person).result?.size

        assertEq(
            expected = documents.size,
            actual = insertedDocumentCount,
        ) {
            "Some of the setup documents were not properly inserted into the database!"
        }

        val batchSuccessful = db.typedBatch {
            delete(document = person1)
            delete(documents = listOf(person2, person3))
            delete(id = person5.id)
            delete(ids = listOf(person10.id, person15.id))
        }.isSuccess

        assert(batchSuccessful) { "The batch delete should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Batch delete should have deleted all but one document from the database!"
        }
    }

    @JsName("test24")
    @Test
    fun `Typed batch delete by ID`() = runTest {
        val documents = setOf(person1, person2, person10, person15)
        val expectedDocuments = setOf(person2)

        val putSuccessful = db.put(documents = documents).isSuccess

        assert(putSuccessful) { "The setup put should have been successful!" }

        val insertedDocumentCount = db.getAll(Person).result?.size

        assertEq(
            expected = documents.size,
            actual = insertedDocumentCount,
        ) {
            "Some of the setup documents were not properly inserted into the database!"
        }

        val batchSuccessful = db.typedBatch<Person> {
            deleteByIDPrefix(prefix = "1")
        }.isSuccess

        assert(batchSuccessful) { "The batch delete by id should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Batch delete by id should have deleted all but one document from the database!"
        }
    }

    @JsName("test25")
    @Test
    fun `Put and delete in same typed batch`() = runTest {
        val documents = setOf(person1, person2, person3)
        val expectedDocuments = setOf(person1, person3)

        val batchSuccessful = db.typedBatch {
            put(documents = documents)
            delete(document = person2)
        }.isSuccess

        assert(batchSuccessful) { "The batch should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "All the documents but person2 should have been inserted into the database!"
        }
    }

    @JsName("test26")
    @Test
    fun `Put and delete by ID in same typed batch`() = runTest {
        val documents = setOf(person1, person2, person10, person15)
        val expectedDocuments = setOf(person2)

        val batchSuccessful = db.typedBatch {
            put(documents = documents)
            deleteByIDPrefix(prefix = "1")
        }.isSuccess

        assert(batchSuccessful) { "The batch should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Only person2 should have been inserted into the database!"
        }
    }

    @JsName("test27")
    @Test
    fun `Put delete and delete by ID in same typed batch`() = runTest {
        val documents = setOf(person1, person2, person3, person10, person15)
        val expectedDocuments = setOf(person2)

        val batchSuccessful = db.typedBatch {
            put(documents = documents)
            deleteByIDPrefix(prefix = "1")
            delete(document = person3)
        }.isSuccess

        assert(batchSuccessful) { "The batch should have succeeded!" }

        val actualDocuments = db.getAll(Person).result?.cleanForComparison()

        assertEq(
            expected = expectedDocuments,
            actual = actualDocuments,
        ) {
            "Only person2 should have been inserted into the database!"
        }
    }

    @JsName("test28")
    @Test
    fun `Put is rejected if changeVector doesn't match the database`() = runTest {
        val person1WithChangeVector = db.put(document = person1).result
        person1WithChangeVector.assertPresentInDB()

        val person1WithNameChange = person1WithChangeVector.copy(firstName = "Garrrrrrrrison")
        val person1WithCountryChange = person1WithChangeVector.copy(country = id(record = countryTempest))

        val nameChangeResult = db.put(document = person1WithNameChange)
        assert(nameChangeResult.isSuccess) { "Name change was not successful!" }

        val countryChangeError = db.put(document = person1WithCountryChange).error
        assert(countryChangeError is KorvusError.Raven) { "Country change should have failed with a RavenDB error!" }

        assertEq(
            expected = "Raven.Client.Exceptions.ConcurrencyException",
            actual = countryChangeError.error.type,
        )
    }

    @JsName("test29")
    @Test
    fun `Batch put is rejected if changeVector doesn't match the database`() = runTest {
        val person1WithChangeVector = db.put(document = person1).result
        person1WithChangeVector.assertPresentInDB()

        val person1WithNameChange = person1WithChangeVector.copy(firstName = "Garrrrrrrrison")
        val person1WithCountryChange = person1WithChangeVector.copy(country = id(record = countryTempest))

        val nameChangeResult = db.put(document = person1WithNameChange)
        assert(nameChangeResult.isSuccess) { "Name change was not successful!" }

        val countryChangeError = db.batch {
            put<Person>(document = person1WithCountryChange)
        }.error
        assert(countryChangeError is KorvusError.Raven) { "Country change should have failed with a RavenDB error!" }

        assertEq(
            expected = "Raven.Client.Exceptions.ConcurrencyException",
            actual = countryChangeError.error.type,
        )
    }

    @JsName("test30")
    @Test
    fun `Failure during batch causes all batch operations to be reverted if applied and skipped otherwise`() = runTest {
        val expectedDocuments = setOf(person1, person10)
        val putResult = db.put(document = person10).result
        assertEq(
            expected = person10,
            actual = putResult?.cleanForComparison(),
        ) {
            "The setup put should have been successful!"
        }
        val person1WithChangeVector = db.put(document = person1).result
        person1WithChangeVector.assertPresentInDB()

        val person1WithNameChange = person1WithChangeVector.copy(firstName = "Garrrrrrrrison")
        val person1WithCountryChange = person1WithChangeVector.copy(country = id(record = countryTempest))

        val batchResult = db.batch {
            delete<Person>(id = "10")
            put(document = person1WithNameChange)
            put(document = person1WithCountryChange)
        }.error

        assert(batchResult is KorvusError.Raven) { "Country change should have failed with a RavenDB error!" }
        assertEq(
            expected = "Raven.Client.Exceptions.ConcurrencyException",
            actual = batchResult.error.type,
        )

        assertEq(
            expected = expectedDocuments,
            actual = db.getAll(Person).result?.cleanForComparison(),
        ) {
            "The batch shouldn't have affected the database!"
        }
    }

    @JsName("test31")
    @Test
    fun `Failure during typed batch causes all batch operations to be reverted if applied and skipped otherwise`() = runTest {
        val expectedDocuments = setOf(person1, person10)
        val putResult = db.put(document = person10).result
        assertEq(
            expected = person10,
            actual = putResult?.cleanForComparison(),
        ) {
            "The setup put should have been successful!"
        }
        val person1WithChangeVector = db.put(document = person1).result
        person1WithChangeVector.assertPresentInDB()

        val person1WithNameChange = person1WithChangeVector.copy(firstName = "Garrrrrrrrison")
        val person1WithCountryChange = person1WithChangeVector.copy(country = id(record = countryTempest))

        val batchResult = db.typedBatch {
            delete(id = "10")
            put(document = person1WithNameChange)
            put(document = person1WithCountryChange)
        }.error

        assert(batchResult is KorvusError.Raven) { "Country change should have failed with a RavenDB error!" }
        assertEq(
            expected = "Raven.Client.Exceptions.ConcurrencyException",
            actual = batchResult.error.type,
        )

        assertEq(
            expected = expectedDocuments,
            actual = db.getAll(Person).result?.cleanForComparison(),
        ) {
            "The batch shouldn't have affected the database!"
        }
    }

    @JsName("test32")
    @Test
    fun `Included documents appear in the query result`() = runTest {
        val country = db.put(document = countryUS).result
        assert(country != null) { "Country insertion should have been successful!" }
        val genre = db.put(document = genreSliceOfLife).result
        assert(genre != null) { "Genre insertion should have been successful!" }
        val series = db.put(document = series1.copy(genre = id(record = genre))).result
        assert(series != null) { "Series insertion should have been successful!" }
        val person = db.put(document = person1.copy(country = id(record = country), series = id(record = series))).result
        assert(person != null) { "Person insertion should have been successful!" }
        val result = db.queryOneWithIncludes<Person>(
            query = "from Person where id() = \$id include series, country, series.genre",
            parameters = mapOf("id" to person1.id),
            type = typeOf<Person>(),
        )
    }

    @Serializable
    data class Name(
        val firstName: String,
        @SerialName(value = "LNAME")
        val lastName: String,
    )
}
