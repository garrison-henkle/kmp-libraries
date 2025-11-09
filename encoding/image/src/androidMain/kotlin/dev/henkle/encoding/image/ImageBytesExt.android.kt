package dev.henkle.encoding.image

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.touchlab.kermit.Logger

actual suspend fun ImageBytes.toImageBitmap(): ImageBitmap? =
    try {
        BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
        )?.asImageBitmap()
    } catch (ex: Throwable){
        Logger.e("dev.henkle.encoding.image", throwable = ex) { "Unable to parse bitmap image" }
        null
    }
