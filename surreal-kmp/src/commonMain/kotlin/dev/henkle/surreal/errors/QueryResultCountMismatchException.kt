package dev.henkle.surreal.errors

class QueryResultCountMismatchException(
    expected: Int,
    actual: Int,
) : SurrealSDKResultException("The query results contained $actual results but $expected statements were sent to the database!")
