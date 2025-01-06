package dev.henkle.store.preferences

import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KProperty

interface Preference<T: Any?> {
    val key: String
    var value: T
    val default: T
    val flow: StateFlow<T>
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}
