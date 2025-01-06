package dev.henkle.surreal.errors

class NonSingleStatementQueryException(statementCount: Int) : SurrealSDKArgumentsException(message = "Expected exactly 1 statement in the query but $statementCount were found!")
