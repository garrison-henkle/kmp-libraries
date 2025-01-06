package dev.henkle.nanoid

import kotlin.js.JsName
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.random.Random as RandomKt

class NanoIdTests {
    @Test
    @Ignore
    @ExperimentalStdlibApi
    @JsName("Test100k")
    fun `100k random Nano IDs are unique`() {
        val idCount = 100000
        val ids: MutableSet<String> = HashSet(idCount)
        val nanoId = NanoId()

        for(i in 0 until idCount) {
            val id = nanoId.generate()

            if(!ids.contains(id)) {
                ids.add(id)
            } else {
                fail("Non-unique ID generated: $id")
            }
        }
    }

    @Test
    @ExperimentalStdlibApi
    @JsName("SeededRandom")
    fun `Seeded Pseudorandom number generator generates expected Nano IDs`() {
        val seededPseudoRandom = object : Random {
            val random = RandomKt(seed = 12345)
            override fun copyNextBytesTo(buffer: ByteArray) {
                random.nextBytes(array = buffer)
            }
        }
        val nanoId = NanoId(random = seededPseudoRandom)

        val expectedIds: Array<String> = arrayOf(
            "CBf3MTFR_-Yje-f5imwB",
            "J-6TdBAz6_lc9OZ1E1J8",
            "H3dTyOKNz5Q2POgun3BB",
            "p_V_DgrPnjHn9OxAVuJt",
            "OQCnbHGL6r-C5ocjxt6J",
        )

        for(expectedId in expectedIds) {
            val generatedId = nanoId.generate()
            assertEquals(expectedId, generatedId)
        }
    }

    @Test
    @Ignore
    @ExperimentalStdlibApi
    @JsName("SizeCheck")
    fun `Nano IDs can be generated in lengths varying from 1 to 1_000`() {
        for(length in 1 ..1000) {
            val id = NanoId(length = length).generate()

            assertEquals(length, id.length)
        }
    }

    @Test
    @Ignore
    @ExperimentalStdlibApi
    @JsName("WellDistributed")
    fun `Generated Nano IDs are well-distributed`() {
        val idCount = 100000
        val idLength = 20
        val alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        val charCounts: MutableMap<String, Long> = HashMap()
        val nanoId = NanoId(length = idLength, alphabet = alphabet)

        for(i in 0 until idCount) {
            val id = nanoId.generate()

            for(j in id.indices) {
                val value = id[j].toString()
                val charCount: Long? = charCounts[value]

                if(charCount == null) {
                    charCounts[value] = 1L
                } else {
                    charCounts[value] = charCount +1
                }
            }
        }

        for(charCount in charCounts.values) {
            val distribution: Double = (charCount * alphabet.size / (idCount * idLength).toDouble())
            assertTrue(distribution in 0.95..1.05)
        }
    }

    @Test
    @ExperimentalStdlibApi
    @JsName("EmptyAlphabet")
    fun `Building a Nano ID instance with an empty alphabet throws an IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            NanoId(alphabet = charArrayOf())
        }
    }

    @Test
    @ExperimentalStdlibApi
    @JsName("AlphabetTooLong")
    fun `Building a Nano ID instance with an alphabet that is too long throws an IllegalArgumentException`() {
        val largeAlphabet = CharArray(256) { i -> i.toChar() }

        assertFailsWith<IllegalArgumentException> {
            NanoId(alphabet = largeAlphabet)
        }
    }

    @Test
    @ExperimentalStdlibApi
    @JsName("NegativeLength")
    fun `Building a Nano ID instance with a negative length throws an IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            NanoId(length = -10)
        }
    }

    @Test
    @ExperimentalStdlibApi
    @JsName("ZeroLength")
    fun `Building a Nano ID instance with 0 length throws an IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            NanoId(length = 0)
        }
    }
}