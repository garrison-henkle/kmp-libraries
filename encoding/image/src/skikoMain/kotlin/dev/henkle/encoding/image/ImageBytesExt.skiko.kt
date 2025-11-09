package dev.henkle.encoding.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image

actual suspend fun ImageBytes.toImageBitmap(): ImageBitmap? = withContext(Dispatchers.IO){
    try {
        Image.makeFromEncoded(bytes = bytes).toComposeImageBitmap()
    } catch (ex: Throwable){
        Logger.e("dev.henkle.encoding.image", throwable = ex) { "Unable to parse bitmap image" }
        null
    }
}
