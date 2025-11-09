package dev.henkle.encoding.image

import co.touchlab.kermit.Logger
import com.ditchoom.buffer.ByteOrder
import com.ditchoom.buffer.PlatformBuffer
import com.ditchoom.buffer.wrap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decoder for the .ico image type
 *
 * Supports any number of embedded BMP (DIB) or PNG images. Selecting between images within a single
 * .ico container can be achieved using the preferred size ([IconSize]) and size strategy
 * ([SizeMatchingStrategy]) parameters of the decoder function.
 *
 * Based on https://github.com/drampelt/coil-ico/blob/016e78bae7f596d86e0d196fa6a3bff29c293034/coil-ico/src/main/java/com/danielrampelt/coil/ico/IcoDecoder.kt
 * Modified with additional information found at https://en.wikipedia.org/wiki/ICO_(file_format)
 *
 * @see BmpEncoding for more information on how embedded BMPs are decoded.
 */
object IcoEncoding {
    private const val PNG_MAGIC_BYTE_1 = 0x89.toByte()
    private const val PNG_MAGIC_BYTE_2 = 0x50.toByte()
    private const val PNG_MAGIC_BYTE_3 = 0x4E.toByte()
    private const val PNG_MAGIC_BYTE_4 = 0x47.toByte()

    /**
     * Decodes the .ico represented by [bytes]. If multiple images are stored in the .ico,
     * the image matching the [preferredSize] will be returned. If no matches are found, the
     * largest image is returned.
     */
    suspend fun decode(
        bytes: ByteArray,
        preferredSize: IconSize = IconSize(width = 64, height = 64),
        sizeStrategy: SizeMatchingStrategy = SizeMatchingStrategy.ExactOrNextLargest,
    ): ImageBytes? = withContext(context = Dispatchers.IO) {
        try{
            val buffer = PlatformBuffer.wrap(array = bytes, byteOrder = ByteOrder.LITTLE_ENDIAN)
            buffer.run{
                readUnsignedInt()
                val numImages = readUnsignedShort().toInt()
                val images = List(numImages) { IconDirEntry.parse(this) }

                val bestImage = when(sizeStrategy) {
                    SizeMatchingStrategy.Largest -> images.maxBy { it.width }
                    SizeMatchingStrategy.Smallest -> images.maxBy { it.height }
                    SizeMatchingStrategy.ExactOrNextLargest -> images
                        .sortedBy { it.width }
                        .run{
                            indexOfFirst { it.width >= preferredSize.width }
                                .takeIf { it != -1 }
                                ?.let { images[it] }
                                ?: last()
                        }
                    SizeMatchingStrategy.ExactOrNextSmallest -> images
                        .sortedByDescending { it.width }
                        .run{
                            indexOfFirst { it.width <= preferredSize.width }
                                .takeIf { it != -1 }
                                ?.let { images[it] }
                                ?: last()
                        }
                }
                val imageBytes = bytes.copyOfRange(bestImage.offset, bestImage.offset + bestImage.size)

                val type = if(
                    imageBytes.size > 4 &&
                    imageBytes[0] == PNG_MAGIC_BYTE_1 &&
                    imageBytes[1] == PNG_MAGIC_BYTE_2 &&
                    imageBytes[2] == PNG_MAGIC_BYTE_3 &&
                    imageBytes[3] == PNG_MAGIC_BYTE_4
                ) {
                    MimeType.PNG
                } else {
                    MimeType.BMP
                }
                ImageBytes(bytes = imageBytes, type = type)
            }
        } catch(ex: Throwable){
            Logger.e("dev.henkle.encoding.image", throwable = ex) { "Unable to decode the .ico file!" }
            null
        }
    }

    private data class IconDirEntry(
        val width: Int,
        val height: Int,
        val numColors: UByte,
        val colorPlanes: UShort,
        val bytesPerPixel: UShort,
        val size: Int,
        val offset: Int,
    ){
        constructor(
            _width: UByte,
            _height: UByte,
            numColors: UByte,
            colorPlanes: UShort,
            bytesPerPixel: UShort,
            size: Int,
            offset: Int,
        ) : this(
            width = _width.toInt().takeUnless { it == 0 } ?: 256,
            height = _height.toInt().takeUnless { it == 0 } ?: 256,
            numColors = numColors,
            colorPlanes = colorPlanes,
            bytesPerPixel = bytesPerPixel,
            size = size,
            offset = offset,
        )

        companion object{
            fun parse(reader: PlatformBuffer): IconDirEntry = reader.run{
                val width = readUnsignedByte()
                val height = readUnsignedByte().toUInt()
                val numColors = readUnsignedByte()
                readUnsignedByte() //ignore reserved byte
                val colorPlanes = readUnsignedShort()
                val bytesPerPixel = readUnsignedShort()
                val size = readUnsignedInt()
                val offset = readUnsignedInt()
                IconDirEntry(
                    _width = width,
                    _height = height.toUByte(),
                    numColors = numColors,
                    colorPlanes = colorPlanes,
                    bytesPerPixel = bytesPerPixel,
                    size = size.toInt(),
                    offset = offset.toInt(),
                )
            }
        }
    }
}
