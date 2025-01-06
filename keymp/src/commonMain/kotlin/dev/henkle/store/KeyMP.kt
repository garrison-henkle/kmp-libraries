package dev.henkle.store

@Suppress("MemberVisibilityCanBePrivate")
object KeyMP {
    val platformStorageProviders: PlatformStorageProviders by lazy { getPlatformStorageProviders() }
    val defaultStorage: Storage by lazy { platformStorageProviders.getPlatformDefaultStorage() }
    val secureStorage: Storage by lazy { platformStorageProviders.getPlatformSecureStorage() }
}
