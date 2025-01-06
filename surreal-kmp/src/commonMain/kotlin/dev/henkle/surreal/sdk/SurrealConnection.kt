package dev.henkle.surreal.sdk

class SurrealConnection {
    val user: String
    val password: String
    val namespace: String?
    val database: String?

    constructor(user: String, password: String) {
        this.user = user
        this.password = password
        this.namespace = null
        this.database = null
    }

    constructor(user: String, password: String, namespace: String) {
        this.user = user
        this.password = password
        this.namespace = namespace
        this.database = null
    }

    constructor(user: String, password: String, namespace: String, database: String) {
        this.user = user
        this.password = password
        this.namespace = namespace
        this.database = database
    }
}
