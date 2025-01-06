package dev.henkle.stytch.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val IODispatcher: CoroutineDispatcher = Dispatchers.IO
