package dev.henkle.surreal.sdk

import kotlinx.serialization.json.JsonElement

typealias RawSurrealQueryResult = SurrealResult<List<SurrealQueryResult<List<JsonElement>?>>>
