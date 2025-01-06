package dev.henkle.test

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertEquals
import kotlin.test.fail

fun <T> assertEq(expected: T, actual: T, lazyMessage: (() -> String)? = null) {
    try {
        assertEquals(
            expected = expected,
            actual = actual,
        )
    } catch (ex: AssertionError) {
        lazyMessage?.invoke()?.also(::printlnToStdErr)
        throw ex
    }
}

@OptIn(ExperimentalContracts::class)
fun assert(condition: Boolean, lazyMessage: (() -> String)? = null) {
    contract {
        returns() implies condition
    }
    if (!condition) {
        fail("Assertion failed! ${lazyMessage?.invoke() ?: ""}")
    }
}

expect fun printToStdErr(msg: String)

fun printlnToStdErr(msg: Any) = printToStdErr("$msg\n")

expect fun executeCommand(command: String): ProcessOutput
