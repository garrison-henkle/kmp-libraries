package dev.henkle.utils

expect fun getPlatform(): Platform

expect fun printToStdErr(msg: String)

fun printlnToStdErr(msg: String) = printToStdErr("$msg\n")
