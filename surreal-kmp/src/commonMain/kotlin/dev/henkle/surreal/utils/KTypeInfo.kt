package dev.henkle.surreal.utils

import io.ktor.util.reflect.TypeInfo
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class KTypeInfo<T: Any>(val clazz: KClass<T>, val type: KType) {
    internal fun asTypeInfo(): TypeInfo = TypeInfo(type = clazz, kotlinType = type)
}

inline fun <reified T: Any> kTypeInfo(): KTypeInfo<T> = KTypeInfo(clazz = T::class, type = typeOf<T>())
