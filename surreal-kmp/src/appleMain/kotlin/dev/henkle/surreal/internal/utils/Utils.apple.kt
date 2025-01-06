package dev.henkle.surreal.internal.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual val IODispatcher: CoroutineDispatcher = Dispatchers.IO
