package dev.henkle.datastructures.queue

/**
 * Creates an empty [Queue] instance
 */
fun <T: Any> Queue(): Queue<T> = QueueImpl()

/**
 * Creates an empty [MutableQueue] instance
 */
fun <T: Any> MutableQueue(): MutableQueue<T> = QueueImpl()
