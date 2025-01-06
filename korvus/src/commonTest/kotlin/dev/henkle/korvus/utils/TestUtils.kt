package dev.henkle.korvus.utils

import dev.henkle.korvus.types.KorvusDocument
import dev.henkle.test.assert
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun <T: KorvusDocument<T>> T.cleanForComparison(): T =
    update(changeVector = null, lastModified = null)

fun <T: KorvusDocument<T>> Iterable<T>.cleanForComparison(): Set<T> =
    map { it.cleanForComparison() }.toSet()

@OptIn(ExperimentalContracts::class)
fun <T: KorvusDocument<T>> T?.assertPresentInDB() {
    contract {
        returns() implies (this@assertPresentInDB != null)
    }
    assert(this != null && changeVector != null && lastModified != null) {
        "Document is missing its change vector or last modified date!"
    }
}

@OptIn(ExperimentalContracts::class)
fun <T: KorvusDocument<T>> Iterable<T>?.assertPresentInDB() {
    contract {
        returns() implies (this@assertPresentInDB != null)
    }
    this?.forEach { it.assertPresentInDB() }
        ?: assert(false) { "Documents are missing change vectors or last modified dates!" }
}
