/**
 * MIT License
 *
 * Copyright (c) 2020 DatLag
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.henkle.nanoid

import kotlin.experimental.and
import kotlin.jvm.JvmStatic
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log

/**
 * A Nano ID generator slightly tweaked from
 * ([DatL4g's KMP implementation](https://github.com/DatL4g/KMP-NanoId)).
 *
 * [original Nano ID implementation](https://github.com/ai/nanoid)
 *
 * [collision calculator](https://zelark.github.io/nano-id-cc/)
 *
 * @author DatL4g
 * @author Garrison Henkle
 */
class NanoId(
    private val random: Random = Random.default,
    private val alphabet: CharArray = DEFAULT_ALPHABET,
    private val length: Int = DEFAULT_LENGTH,
) {
    init {
        require(alphabet.size in 1..255) {
            "ID alphabet must contain between 1 and 255 symbols!"
        }
        require(length > 0) {
            "ID length must be greater than zero!"
        }
    }

    fun generate(): String {
        val mask: Int = (2 shl floor(log((alphabet.size - 1).toDouble(), 2.0)).toInt()) - 1
        val step: Int = ceil(1.6 * mask * length / alphabet.size).toInt()
        val idBuilder = StringBuilder()

        while (true) {
            val bytes = ByteArray(step)
            random.copyNextBytesTo(buffer = bytes)

            for (i in 0 until step) {
                val alphabetIndex: Int = (bytes[i] and mask.toByte()).toInt()

                if (alphabetIndex < alphabet.size) {
                    idBuilder.append(alphabet[alphabetIndex])

                    if (idBuilder.length == length) {
                        return idBuilder.toString()
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        private val DEFAULT_ALPHABET: CharArray =
            "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

        // At 20 characters, it would take 5 million years of generating 1,000 IDs per second
        // (163,457 trillion IDs) for there to be a 1% chance of collision
        private const val DEFAULT_LENGTH = 20

        @JvmStatic
        val DEFAULT = NanoId()
    }
}

fun nanoId(): String = NanoId.DEFAULT.generate()
