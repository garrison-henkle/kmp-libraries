package dev.henkle.datastructures.stack

/**
 * Creates an empty [Stack] instance
 */
fun <T> Stack(): Stack<T> = StackImpl()

/**
 * Creates an empty [MutableStack] instance
 */
fun <T> MutableStack(): MutableStack<T> = StackImpl()
