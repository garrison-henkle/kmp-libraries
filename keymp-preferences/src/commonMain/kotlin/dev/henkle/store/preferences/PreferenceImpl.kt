package dev.henkle.store.preferences

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

internal class PreferenceImpl<T>(
    private val container: PreferencesContainer,
    override val key: String,
    override val default: T,
    get: (key: String, default: T) -> T,
    private val set: (key: String, value: T) -> Unit,
): Preference<T> {
    private val _flow = MutableStateFlow(get(key, default))
    override val flow: StateFlow<T> = _flow
    override var value: T
        get() = _flow.value
        set(newValue) {
            _flow.value = newValue
            container.queue.trySend(
                container.scope.launch(start = CoroutineStart.LAZY) {
                    set(key, newValue)
                }
            )
        }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { this.value = value }
}
