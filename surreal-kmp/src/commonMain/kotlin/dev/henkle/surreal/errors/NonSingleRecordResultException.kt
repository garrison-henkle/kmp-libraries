package dev.henkle.surreal.errors

class NonSingleRecordResultException(recordCount: Int) : SurrealSDKResultException(message = "queryOne result should contain exactly one record but $recordCount were found!")
