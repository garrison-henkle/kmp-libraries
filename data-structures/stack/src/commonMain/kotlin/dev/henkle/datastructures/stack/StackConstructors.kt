package dev.henkle.datastructures.stack

/**
 * Creates an empty [Stack] instance
 */
fun <T: Any> Stack(): Stack<T> = StackImpl()

/**
 * Creates an empty [MutableStack] instance
 */
fun <T: Any> MutableStack(): MutableStack<T> = StackImpl()
