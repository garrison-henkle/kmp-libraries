package dev.henkle.encoding.image

import android.graphics.Bitmap
import android.graphics.ColorSpace.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.ditchoom.buffer.ByteOrder
import com.ditchoom.buffer.PlatformBuffer
import com.ditchoom.buffer.wrap

//modified version of androidx.compose.ui.graphics.AndroidImageBitmap
actual fun ImageBitmap(
    width: Int,
    height: Int,
    imageData: ByteArray,
    colorDataOffset: Int,
    config: ImageBitmapConfig,
    hasAlpha: Boolean,
    colorSpace: ColorSpace,
) : ImageBitmap {
    val bitmap = Bitmap.createBitmap(
        null,
        width,
        height,
        config.toBitmapConfig(),
        hasAlpha,
        colorSpace.toFrameworkColorSpace()
    )

    val buffer = PlatformBuffer.wrap(
        array = imageData,
        byteOrder = ByteOrder.LITTLE_ENDIAN,
    )
    val bytesPerPixel = when(config){
        ImageBitmapConfig.Argb8888 -> 4
        else -> throw NotImplementedError()
    }
    val colorInts = IntArray(width * height)
    var index = 0
    for (y in height - 1 downTo 0){
        buffer.position(width * bytesPerPixel * y + colorDataOffset)
        repeat(times = width){
            colorInts[index++] = buffer.readInt()
        }
    }
    bitmap.setPixels(
        colorInts,
        0,
        width,
        0,
        0,
        width,
        height,
    )
    return bitmap.asImageBitmap()
}

private fun ColorSpace.toFrameworkColorSpace(): android.graphics.ColorSpace {
    val frameworkNamedSpace = when (this) {
        ColorSpaces.Srgb -> Named.SRGB
        ColorSpaces.Aces -> Named.ACES
        ColorSpaces.Acescg -> Named.ACESCG
        ColorSpaces.AdobeRgb -> Named.ADOBE_RGB
        ColorSpaces.Bt2020 -> Named.BT2020
        ColorSpaces.Bt709 -> Named.BT709
        ColorSpaces.CieLab -> Named.CIE_LAB
        ColorSpaces.CieXyz -> Named.CIE_XYZ
        ColorSpaces.DciP3 -> Named.DCI_P3
        ColorSpaces.DisplayP3 -> Named.DISPLAY_P3
        ColorSpaces.ExtendedSrgb -> Named.EXTENDED_SRGB
        ColorSpaces.LinearExtendedSrgb ->
            Named.LINEAR_EXTENDED_SRGB
        ColorSpaces.LinearSrgb -> Named.LINEAR_SRGB
        ColorSpaces.Ntsc1953 -> Named.NTSC_1953
        ColorSpaces.ProPhotoRgb -> Named.PRO_PHOTO_RGB
        ColorSpaces.SmpteC -> Named.SMPTE_C
        else -> Named.SRGB
    }
    return get(frameworkNamedSpace)
}

private fun ImageBitmapConfig.toBitmapConfig(): Bitmap.Config {
    // Cannot utilize when statements with enums that may have different sets of supported
    // values between the compiled SDK and the platform version of the device.
    // As a workaround use if/else statements
    // See https://youtrack.jetbrains.com/issue/KT-30473 for details
    return if (this == ImageBitmapConfig.Argb8888) {
        Bitmap.Config.ARGB_8888
    } else if (this == ImageBitmapConfig.Alpha8) {
        Bitmap.Config.ALPHA_8
    } else if (this == ImageBitmapConfig.Rgb565) {
        Bitmap.Config.RGB_565
    } else if (this == ImageBitmapConfig.F16) {
        Bitmap.Config.RGBA_F16
    } else if (this == ImageBitmapConfig.Gpu) {
        Bitmap.Config.HARDWARE
    } else {
        Bitmap.Config.ARGB_8888
    }
}
