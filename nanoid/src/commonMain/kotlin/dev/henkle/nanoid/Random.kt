package dev.henkle.nanoid

import org.kotlincrypto.SecureRandom

interface Random {
    fun copyNextBytesTo(buffer: ByteArray)

    companion object {
        internal val default = object : Random {
            val random = SecureRandom()
            override fun copyNextBytesTo(buffer: ByteArray) = random.nextBytesCopyTo(bytes = buffer)
        }
    }
}
