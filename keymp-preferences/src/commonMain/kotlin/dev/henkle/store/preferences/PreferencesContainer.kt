package dev.henkle.store.preferences

import dev.henkle.store.KeyMP
import dev.henkle.store.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

abstract class PreferencesContainer(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val store: Storage = KeyMP.defaultStorage
    val queue: Channel<Job> = Channel(capacity = Channel.UNLIMITED)

    fun start() {
        scope.launch {
            for(job in queue) job.join()
        }
    }

    fun stop() {
        queue.cancel()
        scope.cancel()
    }

    fun stringPreference(key: String, default: String): Preference<String> =
        PreferenceImpl(container = this, key = key, default = default, get = store::getString, set = store::set)

    fun booleanPreference(key: String, default: Boolean): Preference<Boolean> =
        PreferenceImpl(container = this, key = key, default = default, get = store::getBoolean, set = store::set)

    fun intPreference(key: String, default: Int): Preference<Int> =
        PreferenceImpl(container = this, key = key, default = default, get = store::getInt, set = store::set)

    fun longPreference(key: String, default: Long): Preference<Long> =
        PreferenceImpl(container = this, key = key, default = default, get = store::getLong, set = store::set)

    fun floatPreference(key: String, default: Float): Preference<Float> =
        PreferenceImpl(container = this, key = key, default = default, get = store::getFloat, set = store::set)

    fun doublePreference(key: String, default: Double): Preference<Double> =
        PreferenceImpl(container = this, key = key, default = default, get = store::getDouble, set = store::set)

    fun <T> objectPreference(
        key: String,
        default: T,
        serialize: (T) -> String,
        deserialize: (String) -> T,
    ): Preference<T> = object : Preference<T> {
        private val backing = stringPreference(key = key, default = serialize(default))

        override val key: String = key
        override val default: T = default
        private val _flow = MutableStateFlow(deserialize(backing.value))
        override val flow: StateFlow<T> = _flow
        override var value: T
            get() = _flow.value
            set(newValue) {
                _flow.value = newValue
                queue.trySend(
                    scope.launch(start = CoroutineStart.LAZY) {
                        backing.value = serialize(newValue)
                    }
                )
            }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { this.value = value }
    }

    inline fun <reified T: Enum<T>> enumPreference(
        key: String,
        default: T,
    ): Preference<T> = objectPreference(
        key = key,
        default = default,
        serialize = { it.name },
        deserialize = { enumValueOf(it) },
    )
}
