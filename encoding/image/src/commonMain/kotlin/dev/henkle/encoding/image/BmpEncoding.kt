package dev.henkle.encoding.image

import androidx.compose.ui.graphics.ImageBitmap
import com.ditchoom.buffer.PlatformBuffer
import com.ditchoom.buffer.wrap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Encoding utils for Windows bitmaps (.bmp) and device independent bitmaps (DIB).
 *
 * Only 32-bit color BITMAPINFOHEADER BMP/DIBs are explicitly supported, but other styles of BMP/DIB
 * that share the BITMAPINFOHEADER should also work.
 *
 * BMP and BITMAPINFOHEADER DIB header formats found here:
 * https://en.wikipedia.org/wiki/BMP_file_format
 */
object BmpEncoding {
    private const val BMP_HEADER_SIZE = 14
    private const val DIP_HEADER_SIZE = 40
    private const val TOTAL_HEADER_SIZE = (BMP_HEADER_SIZE + DIP_HEADER_SIZE).toByte()
    private const val MAGIC_BYTE_1 = 0x42.toByte()
    private const val MAGIC_BYTE_2 = 0x4D.toByte()

    suspend fun decodeToBitmap(bytes: ByteArray): ImageBitmap? = PlatformBuffer
        .wrap(array = bytes)
        .run {
            stripBMPHeaderIfPresent(bytes = bytes)
            BitmapDibHeader.parse(this)?.let { header ->
                // I'm just copying this file from my old repos verbatim for now,
                // but I think there is something wrong here? I think the ImageBytes
                // approach of feeding the data into BitmapFactory.decodeByteArray (on
                // Android) would work fine here for non-rebuilt BMPs. Maybe Skia can't
                // do the same? Regardless, this should be revisited if this is ever used
                // again...
                if (header.bitCount == 32.toUShort()){
                    decodeToBitmap(header = header, imageBytes = bytes, colorDataOffset = position())
                } else {
                    ImageBytes(bytes = rebuildFromIco(bytes), type = MimeType.BMP).toImageBitmap()
                }
            }
        }

    private fun PlatformBuffer.stripBMPHeaderIfPresent(bytes: ByteArray) {
        if (bytes.size >= 2 && bytes[0] == MAGIC_BYTE_1 && bytes[2] == MAGIC_BYTE_2){
            repeat(times = BMP_HEADER_SIZE) { readByte() }
        }
    }

    private suspend fun decodeToBitmap(
        header: BitmapDibHeader,
        imageBytes: ByteArray,
        colorDataOffset: Int,
    ): ImageBitmap? =
        withContext(Dispatchers.IO) {
            try {
                ImageBitmap(
                    width = header.width,
                    height = header.height / 2,
                    imageData = imageBytes,
                    colorDataOffset = colorDataOffset,
                )
            } catch (_: Exception) {
                null
            }
        }

    /**
     * Rebuild a valid .bmp file from an .ico embedded .bmp
     *
     * The .ico standard format calls for the BMP header of the .bmp to be removed. This function
     * rebuilds the BMP header and appends the DIB header / image data extracted from the .ico
     */
    private suspend fun rebuildFromIco(bytes: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        val bitmapTotalByteCount = BMP_HEADER_SIZE + bytes.size
        val (countByte0, countByte1, countByte2, countByte3) = bitmapTotalByteCount.toBytes()
        ByteArray(size = bitmapTotalByteCount).apply {
            this[0] = MAGIC_BYTE_1
            this[1] = MAGIC_BYTE_2
            this[2] = countByte0
            this[3] = countByte1
            this[4] = countByte2
            this[5] = countByte3
            //6-9 are unused and left as 0
            this[10] = TOTAL_HEADER_SIZE
            //10-13 are the data offset, but only the least sig byte (10) is used; rest are 0
            bytes.copyInto(destination = this, destinationOffset = BMP_HEADER_SIZE)
        }
    }

    private data class Color(val red: UByte, val green: UByte, val blue: UByte, val reserved: UByte)

    private data class IntBytes(val byte0: Byte, val byte1: Byte, val byte2: Byte, val byte3: Byte)

    /**
     * Converts an Int to little endian bytes.
     *
     * Treats the Int as a UInt, so errors occur if negative
     */
    private fun Int.toBytes(): IntBytes {
        val byte0 = (this shr 24).toByte()
        val byte1 = (this shr 16).toByte()
        val byte2 = (this shr 8).toByte()
        val byte3 = this.toByte()
        return IntBytes(byte0, byte1, byte2, byte3)
    }

    private data class BitmapDibHeader(
        val headerSize: UInt,
        val width: Int,
        //Note that the height of the BMP image must be twice the height declared in the
        //(.ico) image directory. The second half of the bitmap should be an AND mask for the
        //existing screen pixels, with the output pixels given by the formula:
        //Output = (Existing AND Mask) XOR Image                     - .ico Wikipedia
        val height: Int,
        val colorPlanes: UShort,
        val bitCount: UShort,
        val compression: UInt,
        val imageSize: UInt,
        val pixelsXPerMeter: UInt,
        val pixelsYPerMeter: UInt,
        val colorsUsed: UInt,
        val colorsImportant: UInt,
        val colors: List<Color>,
    ){
        companion object{
            fun parse(buffer: PlatformBuffer): BitmapDibHeader? = buffer.run {
                buffer.position(newPosition = 0)
                val headerSize = readUnsignedInt()
                val width = readInt()
                val height = readInt()
                val colorPlanes = readUnsignedShort()
                val bitCount = readUnsignedShort()
                val compression = readUnsignedInt()
                val imageSize = readUnsignedInt()
                val pixelsXPerMeter = readUnsignedInt()
                val pixelsYPerMeter = readUnsignedInt()
                val colorsUsed = readUnsignedInt()
                val colorsImportant = readUnsignedInt()
                if (compression == 0.toUInt()){
                    val colors: List<Color> = if(bitCount <= 8.toUShort()) {
                        val colorCount = if (colorsUsed == 0.toUInt()) {
                            1 shl bitCount.toInt()
                        } else {
                            colorsUsed.toInt()
                        }
                        (0 ..<colorCount).map {
                            val b = readUnsignedByte()
                            val g = readUnsignedByte()
                            val r = readUnsignedByte()
                            readUnsignedByte()
                            Color(r, g, b, 0xFFu)
                        }
                    } else {
                        emptyList()
                    }
                    BitmapDibHeader(
                        headerSize = headerSize,
                        width = width,
                        height = height,
                        colorPlanes = colorPlanes,
                        bitCount = bitCount,
                        compression = compression,
                        imageSize = imageSize,
                        pixelsXPerMeter = pixelsXPerMeter,
                        pixelsYPerMeter = pixelsYPerMeter,
                        colorsUsed = colorsUsed,
                        colorsImportant = colorsImportant,
                        colors = colors,
                    )
                } else {
                    null
                }
            }
        }
    }
}
