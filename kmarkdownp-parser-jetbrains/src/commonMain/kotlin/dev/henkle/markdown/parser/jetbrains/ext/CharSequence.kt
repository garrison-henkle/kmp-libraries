package dev.henkle.markdown.parser.jetbrains.ext

internal val CharSequence.level: Int
    get() {
        var sum = 0
        for (i in indices.reversed()) {
            sum += when(get(index = i)) {
                '\t' -> 4
                ' ' -> 1
                else -> break
            }
        }
        return sum
    }
