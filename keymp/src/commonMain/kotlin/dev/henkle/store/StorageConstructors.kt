package dev.henkle.store

/**
 * Creates a secure key-value storage instance backed by the
 * platform's default secure key-value storage implementation
 */
@Suppress("FunctionName")
fun SecureStorage(): Storage = KeyMP.secureStorage

/**
 * Creates a key-value storage instance backed by the
 * platform's default key-value storage implementation
 */
fun Storage(): Storage = KeyMP.defaultStorage
