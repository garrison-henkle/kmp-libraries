package dev.henkle.markdown.ui.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.aallam.ktoken.Encoding
import com.aallam.ktoken.Tokenizer
import kotlinx.coroutines.flow.flow

//import androidx.compose.ui.window.Window
//import androidx.compose.ui.window.application

private const val ESCAPED_DOLLAR_SIGN = "\\$"
private const val MATH_BLOCK_DELIMITER = "$$"
private const val INLINE_MATH_DELIMITER = "$"
/**
 * Performs the markdown pre-processing.
 *
 * The following replacements are made:
 * - OpenAI LaTeX notation (`\(`, `\)`, `\[`, and `\]`) is replaced with standard markdown math notation
 *     - The parenthesis delimiters are for inline math and should be converted to the standard markdown `$`
 *       delimiter
 *     - The bracket delimiters are for math blocks and should be converted to the standard markdown `$$` delimiter
 * - Escapes dollar signs that need to be escaped
 *     - We can ignore the following classes of dollar signs:
 *         - Already escaped dollar signs e.g. `\$`
 *         - Properly delimited markdown math blocks e.g. `$$`
 *         - Dollar signs followed by a number (which is usually a price) e.g. `$1.00` or `$ 1.00`
 */
private fun String.preprocessMarkdown(): String =
    StringBuilder(length).apply {
        val input = this@preprocessMarkdown
        var i = 0
        var c0: Char
        var c1: Char
        var c2: Char
        if (input.isNotEmpty() && input[0] == '$') {
            if (input.length >= 2 && input[1] == '$') {
                append(MATH_BLOCK_DELIMITER)
                i += 2
            } else {
                append(ESCAPED_DOLLAR_SIGN)
                i++
            }
        }
        while (i < input.length) {
            c0 = input[i]
            when {
                c0 == '$' -> {
                    if (i + 1 < input.length) {
                        c1 = input[i + 1]
                        when {
                            c1 == '$' -> {
                                append(MATH_BLOCK_DELIMITER)
                                i += 2
                                continue
                            }
                            c1.isDigit() -> {
                                append(ESCAPED_DOLLAR_SIGN)
                                append(c1)
                                i += 2
                                continue
                            }
                        }
                    }
                    if (i + 2 < input.length) {
                        c1 = input[i + 1]
                        c2 = input[i + 2]
                        if (c1.isWhitespace() && c2.isDigit()) {
                            append(ESCAPED_DOLLAR_SIGN)
                            append(c1)
                            append(c2)
                            i += 3
                            continue
                        }
                    }
                }
                c0 == '\\' && i + 1 < input.length -> {
                    c1 = input[i + 1]
                    when (c1) {
                        '(', ')' -> {
                            append(INLINE_MATH_DELIMITER)
                            i += 2
                            continue
                        }
                        '[', ']' -> {
                            append(MATH_BLOCK_DELIMITER)
                            i += 2
                            continue
                        }
                        '$' -> {
                            append(ESCAPED_DOLLAR_SIGN)
                            i += 2
                            continue
                        }
                    }
                }
            }
            append(c0)
            i++
        }

    }.toString()

private val BUG = """
    Altana has raised *a total of **$322 million*** in funding as of its most recent Series C round in July 2024.
    This includes a **$200 million Series C round** led by Thomas Tull’s US Innovative Technology Fund, which brought the
    company to a valuation of $1 billion [[1]](https://altana.ai/resources/series-c-valuation)[[2]](https://news.crunchbase.com/venture/supply-chain-startup-unicorn-altana/)[[3]](https://www.forbes.com/sites/richardnieva/2024/07/29/altana-unicorn-fundraise-200-million/). Prior to this,
    Altana raised **$100 million in a Series B round** in 2022.

    Just a link [[1]](https://you.com)
""".trimIndent().preprocessMarkdown().replace(regex = "\\[\\[(.+)]]".toRegex(), replacement = "[$1]")

private val TEST = """
    Just a link [[1]](https://you.com)
""".trimIndent().preprocessMarkdown().replace(regex = "\\[\\[(.+)]]".toRegex(), replacement = "[$1]")

private val BULLETED_LIST_BOLD_FAILING = """
    2. **Subsequent Rounds**:
       - By early 2024, Perplexity had achieved a valuation of **$2.5 billion to $3 billion** while raising at least **$250 million** [[1]](https://techcrunch.com/2024/04/23/perplexity-is-raising-250m-at-2-point-5-3b-valuation-ai-search-sources-say/).
       - The company is currently in the final stages of raising **$500 million**, which is expected to elevate its valuation to **$9 billion** [[2]](https://www.cnbc.com/2024/11/05/perplexity-ai-nears-500-million-funding-round-at-9-billion-valuation.html).
""".trimIndent().preprocessMarkdown().replace(regex = "\\[\\[(.+)]]".toRegex(), replacement = "[$1]")

suspend fun main() {
//    val parser = Parser(language = Language(TreeSitterMarkdown.blockLanguagePointer()))
//    val inlineParser = Parser(language = Language(TreeSitterMarkdown.inlineLanguagePointer()))
//    val tree = parser.parse(source = BUG)
//    tree.walk().apply {
//        println(currentFieldName)
//        println(currentNode.grammarSymbol)
//        println(currentNode.grammarType)
//        println(currentNode.symbol)
//        println(currentNode.type)
//
//        println(currentNode.child(index = 0u)?.child(index = 0u)?.grammarType)
//    }

//    MarkdownParser().parse(source = BULLETED_LIST_BOLD_FAILING)?.also { tree ->
//        val cursor = tree.walk()
//        outer@while (true) {
//            println("node ${cursor.currentNode.type} at [${cursor.currentNode.startByte},${cursor.currentNode.endByte}): ${cursor.currentText}")
//            when {
//                cursor.gotoFirstChild() -> {
////                    println("went to child")
//                }
//                cursor.gotoNextSibling() -> {
////                    println("went to sibling")
//                }
//                else -> {
//                    inner@while (cursor.gotoParent()) {
////                        println("went to parent")
//                        if (cursor.gotoNextSibling()) continue@outer
//                    }
//                    break@outer
//                }
////                cursor.gotoParent() -> {
////                    println("went to parent (${cursor.currentNode.type})")
////                    if (!cursor.gotoNextSibling()) {
////                        if (!cursor.gotoParent())
////                    }
////                    println("went to sibling")
////                }
////                else -> break
//            }
////            println("${cursor.currentNode.type} [${cursor.currentNode.startByte},${cursor.currentNode.endByte}): ${cursor.currentText}")
//        }
//    }

    application {
        Window(onCloseRequest = ::exitApplication, title = "KMarkdownP UI Demo") {
            App()
        }
    }
}
