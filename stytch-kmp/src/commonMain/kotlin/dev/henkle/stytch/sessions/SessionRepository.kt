package dev.henkle.stytch.sessions

import dev.henkle.store.Storage
import dev.henkle.stytch.model.session.SessionKey
import dev.henkle.stytch.utils.IODispatcher
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class SessionRepository(private val storage: Storage) {
    private val scope = CoroutineScope(IODispatcher)
    private val lock = SynchronizedObject()
    private val cache = mutableMapOf<SessionKey, String>()

    var jwt: String?
        get() = get(key = SessionKey.SessionJWT)
        set(token) { set(key = SessionKey.SessionJWT, value = token) }

    var opaque: String?
        get() = get(key = SessionKey.SessionOpaque)
        set(token) { set(key = SessionKey.SessionOpaque, value = token) }

    operator fun get(key: SessionKey): String? = retrieve(key = key)

    operator fun set(key: SessionKey, value: String?) {
        if (value != null) {
            store(key = key, value = value)
        } else {
            clear(key = key)
        }
    }

    fun clear() {
        cache.clear()
        scope.launch {
            synchronized(lock) {
                SessionKey.entries.forEach(action = ::clear)
            }
        }
    }

    private fun retrieve(key: SessionKey): String? = cache[key]
        ?: synchronized(lock) { storage[key.id] }?.also { cache[key] = it }

    private fun store(key: SessionKey, value: String) {
        cache[key] = value
        scope.launch {
            synchronized(lock) {
                storage[key.id] = value
            }
        }
    }

    private fun clear(key: SessionKey) {
        cache.remove(key = key)
        scope.launch {
            synchronized(lock) {
                storage.clear(key = key.id)
            }
        }
    }
}
