package dev.henkle.korvus.error.types

class NotJsonObjectException : Exception(
    "A non-object document was provided as an argument! Please ensure all documents are serialized as Json objects."
)
