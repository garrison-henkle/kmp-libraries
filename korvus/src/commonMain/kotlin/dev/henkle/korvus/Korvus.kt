package dev.henkle.korvus

import dev.henkle.korvus.internal.impl.KorvusImpl

interface Korvus {
    val db: KorvusDatabaseAdmin

    val config: KorvusConfig

    companion object {
        @Suppress("unused")
        fun create(url: String, config: KorvusConfig = KorvusConfig()): Korvus =
            KorvusImpl(baseUrl = url, config = config)
    }
}
