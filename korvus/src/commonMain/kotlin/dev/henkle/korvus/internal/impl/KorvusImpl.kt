package dev.henkle.korvus.internal.impl

import dev.henkle.korvus.Korvus
import dev.henkle.korvus.KorvusConfig
import dev.henkle.korvus.KorvusDatabaseAdmin

internal class KorvusImpl(baseUrl: String, override val config: KorvusConfig) : Korvus {
    override val db: KorvusDatabaseAdmin = KorvusDatabaseAdminImpl(baseUrl = baseUrl, config = config)
}
