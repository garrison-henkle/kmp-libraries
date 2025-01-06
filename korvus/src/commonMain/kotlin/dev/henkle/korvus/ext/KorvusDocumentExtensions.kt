package dev.henkle.korvus.ext

import dev.henkle.korvus.types.DBResult
import dev.henkle.korvus.types.KorvusDocument
import dev.henkle.korvus.types.KorvusMetadata

fun <T: KorvusDocument<T>> T.update(with: DBResult): T? =
    when (with) {
        is DBResult.Put -> update(with = with)
        is DBResult.Delete -> update(with = with)
        // TODO(garrison)
        is DBResult.Patch -> this
    }

fun <T: KorvusDocument<T>> T.update(with: KorvusMetadata): T =
    update(
        id = with.id,
        changeVector = with.changeVector,
        lastModified = with.lastModified,
        collection = collection,
    )

fun <T: KorvusDocument<T>> T.update(with: DBResult.Put): T =
    update(
        id = with.id,
        changeVector = with.changeVector,
        lastModified = with.lastModified,
    )

fun <T: KorvusDocument<T>> T.update(with: DBResult.Delete): T? =
    takeIf { !with.deleted }
