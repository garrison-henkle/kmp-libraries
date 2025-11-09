package dev.henkle.encoding.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo

actual fun ImageBitmap(
    width: Int,
    height: Int,
    imageData: ByteArray,
    colorDataOffset: Int,
    config: ImageBitmapConfig,
    hasAlpha: Boolean,
    colorSpace: androidx.compose.ui.graphics.colorspace.ColorSpace,
): ImageBitmap {
    val imageInfo = ImageInfo(
        width = width,
        height = height,
        colorType = config.toSkiaColorType(),
        alphaType = if(hasAlpha) ColorAlphaType.PREMUL else ColorAlphaType.OPAQUE,
        colorSpace = colorSpace.toSkiaColorSpace(),
    )
    val colorData = imageData.copyOfRange(fromIndex = colorDataOffset, toIndex = imageData.size)
    val image = Image.makeRaster(
        imageInfo = imageInfo,
        bytes = colorData,
        rowBytes = when(config){
            ImageBitmapConfig.Argb8888 -> 4 * width //4 B/px * row px count
            else -> throw NotImplementedError()
        },
    )
    return image.toComposeImageBitmap()
}

//below taken from androidx.compose.ui.graphics.SkiaImageAsset

private fun ImageBitmapConfig.toSkiaColorType() = when (this) {
    ImageBitmapConfig.Argb8888 -> ColorType.N32
    ImageBitmapConfig.Alpha8 -> ColorType.ALPHA_8
    ImageBitmapConfig.Rgb565 -> ColorType.RGB_565
    ImageBitmapConfig.F16 -> ColorType.RGBA_F16
    else -> ColorType.N32
}

// TODO support all color spaces.
//  to do this we need to implement SkColorSpace::MakeRGB in skia
private fun androidx.compose.ui.graphics.colorspace.ColorSpace.toSkiaColorSpace(): ColorSpace {
    return when (this) {
        ColorSpaces.Srgb -> ColorSpace.sRGB
        ColorSpaces.LinearSrgb -> ColorSpace.sRGBLinear
        ColorSpaces.DisplayP3 -> ColorSpace.displayP3
        else -> ColorSpace.sRGB
    }
}