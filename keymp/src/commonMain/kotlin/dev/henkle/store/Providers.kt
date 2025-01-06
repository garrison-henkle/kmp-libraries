package dev.henkle.store

expect fun getPlatformStorageProviders(): PlatformStorageProviders

expect class PlatformStorageProviders {
    fun getPlatformDefaultStorage(): Storage
    fun getPlatformSecureStorage(): Storage
}
