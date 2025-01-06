package dev.henkle.korvus.error.types

class ResponseItemCountMismatchException :
    IllegalStateException("Response item count does not match request item count!")
