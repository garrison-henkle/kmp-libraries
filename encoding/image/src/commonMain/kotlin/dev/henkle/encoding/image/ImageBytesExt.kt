package dev.henkle.encoding.image

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun ImageBytes.toImageBitmap(): ImageBitmap?
