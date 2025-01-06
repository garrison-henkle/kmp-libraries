package dev.henkle.korvus.types

interface KorvusCollection<T: KorvusDocument<T>> {
    val name: String
}
