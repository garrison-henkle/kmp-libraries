package dev.henkle.markdown.parser.jetbrains

import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.model.MarkdownTextAnnotation
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsBlockquote
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsBold
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsBulletedList
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsCodeBlock
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsCodeFence
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsCodeSpan
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsHTMLBlock
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsHeader
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsImage
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsInlineLink
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsInlineMath
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsItalics
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsLinkDefinition
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsLinkReference
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsMathBlock
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsNumberedList
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsSETextHeader
import dev.henkle.markdown.parser.jetbrains.parsers.parseAsTable
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import dev.henkle.markdown.parser.jetbrains.ext.isBlockQuote
import dev.henkle.markdown.parser.jetbrains.ext.isBold
import dev.henkle.markdown.parser.jetbrains.ext.isCodeBlock
import dev.henkle.markdown.parser.jetbrains.ext.isCodeFence
import dev.henkle.markdown.parser.jetbrains.ext.isCodeSpan
import dev.henkle.markdown.parser.jetbrains.ext.isDivider
import dev.henkle.markdown.parser.jetbrains.ext.isEOL
import dev.henkle.markdown.parser.jetbrains.ext.isFile
import dev.henkle.markdown.parser.jetbrains.ext.isFullReferenceLink
import dev.henkle.markdown.parser.jetbrains.ext.isHeader
import dev.henkle.markdown.parser.jetbrains.ext.isHeaderContent
import dev.henkle.markdown.parser.jetbrains.ext.isHtmlBlock
import dev.henkle.markdown.parser.jetbrains.ext.isHtmlTag
import dev.henkle.markdown.parser.jetbrains.ext.isImage
import dev.henkle.markdown.parser.jetbrains.ext.isInlineLink
import dev.henkle.markdown.parser.jetbrains.ext.isInlineMath
import dev.henkle.markdown.parser.jetbrains.ext.isItalic
import dev.henkle.markdown.parser.jetbrains.ext.isItalicDeclaration
import dev.henkle.markdown.parser.jetbrains.ext.isLineHeader
import dev.henkle.markdown.parser.jetbrains.ext.isLineHeaderContent
import dev.henkle.markdown.parser.jetbrains.ext.isLinkDefinition
import dev.henkle.markdown.parser.jetbrains.ext.isListBullet
import dev.henkle.markdown.parser.jetbrains.ext.isListItem
import dev.henkle.markdown.parser.jetbrains.ext.isListNumber
import dev.henkle.markdown.parser.jetbrains.ext.isMathBlock
import dev.henkle.markdown.parser.jetbrains.ext.isOrderedList
import dev.henkle.markdown.parser.jetbrains.ext.isParagraph
import dev.henkle.markdown.parser.jetbrains.ext.isShortReferenceLink
import dev.henkle.markdown.parser.jetbrains.ext.isTable
import dev.henkle.markdown.parser.jetbrains.ext.isText
import dev.henkle.markdown.parser.jetbrains.ext.isTextChar
import dev.henkle.markdown.parser.jetbrains.ext.isUnorderedList
import dev.henkle.markdown.parser.jetbrains.ext.isWhitespace
import dev.henkle.markdown.parser.jetbrains.ext.toDisjointList
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.parser.MarkdownParser
import kotlin.text.Regex.Companion.escapeReplacement

internal class IRGenerator private constructor(
    val text: String,
    val output: MutableList<MarkdownNode>,
    private val fullText: String?,
    internal val isSubParser: Boolean,
    internal val isLinkParser: Boolean,
    private val isBlockquoteParser: Boolean,
    private val isMathParser: Boolean,
    internal val isListParser: Boolean,
    private val listLevel: Int,
    private val consecutiveEOLs: ConsecutiveEOLWrapper,
    internal val syntaxParser: MarkdownParser,
) {
    private val builder = StringBuilder()
    private val annotations = mutableListOf<MarkdownTextAnnotation>()

    internal constructor(
        text: String,
        syntaxParser: MarkdownParser,
        output: MutableList<MarkdownNode> = mutableListOf(),
    ) : this(
        text = text,
        output = output,
        fullText = null,
        isSubParser = false,
        isLinkParser = false,
        isBlockquoteParser = false,
        isMathParser = false,
        isListParser = false,
        listLevel = 0,
        consecutiveEOLs = ConsecutiveEOLWrapper(count = 0),
        syntaxParser = syntaxParser
    )

    fun parse(
        node: ASTNode,
        depth: Int = 0,
        level: Int = 0,
    ): List<MarkdownNode> {
        val type = node.type
        var wasEOL = false
        when {
            type.isFile -> node.children.forEach { child -> parse(node = child, depth = depth + 1) }
            type.isParagraph -> {
                onNonTextNode()
                node.children.forEach { child -> parse(node = child, depth = depth + 1) }
            }
            type.isEOL -> {
                consecutiveEOLs.count++
                if (consecutiveEOLs.count == 2) {
                    onNonTextNode(eol = true)
//                    output += MarkdownNode.LineBreak(raw = "\n\n")
                }
                wasEOL = true
            }
            type.isCodeFence -> {
                onNonTextNode()
                node.parseAsCodeFence(generator = this)
            }
            type.isCodeBlock -> {
                onNonTextNode()
                node.parseAsCodeBlock(generator = this)
            }
            type.isCodeSpan -> {
                onNonTextNode()
                node.parseAsCodeSpan(generator = this)
            }
            type.isHtmlBlock -> {
                onNonTextNode()
                node.parseAsHTMLBlock(generator = this)
            }
            type.isHtmlTag -> {
                onNonTextNode()
                output += MarkdownNode.HTMLTag(
                    raw = getTextString(node = node),
                    html = getTextString(node = node),
                )
            }
            type.isMathBlock -> {
                onNonTextNode()
                node.parseAsMathBlock(generator = this)
            }
            type.isInlineMath -> {
                onNonTextNode()
                node.parseAsInlineMath(generator = this, depth = depth)
            }
            type.isHeader -> {
                onNonTextNode()
                node.parseAsHeader(generator = this)
            }
            type.isHeaderContent -> {
                onNonTextNode()
                val startIndex = if (node.children.firstOrNull()?.type?.isWhitespace == true) 1 else 0
                node.children.forEach(startIndex..<node.children.size) { child ->
                    parse(node = child, depth = depth + 1)
                }
            }
            type.isLineHeader -> {
                onNonTextNode()
                node.parseAsSETextHeader(generator = this)
            }
            type.isLineHeaderContent -> {
                onNonTextNode()
                node.children.forEach { child -> parse(node = child, depth = depth + 1) }
            }
            type.isInlineLink -> {
                onNonTextNode()
                node.parseAsInlineLink(generator = this)
            }
            type.isLinkDefinition -> {
                onNonTextNode()
                node.parseAsLinkDefinition(generator = this)
            }
            type.isShortReferenceLink -> {
                onNonTextNode()
                if (isLinkParser) {
                    node.processAsText()
                } else {
                    node.parseAsLinkReference(generator = this)
                }
            }
            type.isFullReferenceLink -> {
                onNonTextNode()
                node.parseAsLinkReference(generator = this)
            }
            type.isBlockQuote -> {
                onNonTextNode()
                node.parseAsBlockquote(generator = this)
            }
            type.isOrderedList -> {
                onNonTextNode()
                val currentBufferLevel = calculateTrailingSpaceCount()
                node.parseAsNumberedList(
                    generator = this,
                    listLevelOffset = level + listLevel + currentBufferLevel,
                )
            }
            type.isUnorderedList -> {
                onNonTextNode()
                val currentBufferLevel = calculateTrailingSpaceCount()
                node.parseAsBulletedList(
                    generator = this,
                    listLevelOffset = level + listLevel + currentBufferLevel,
                )
            }
            type.isListItem -> {
                onNonTextNode()
                val currentBufferLevel = calculateTrailingSpaceCount()
                node.children.forEach { child ->
                    parse(
                        node = child,
                        depth = depth + 1,
                        level = level + currentBufferLevel,
                    )
                }
            }
            type.isImage -> {
                onNonTextNode()
                node.parseAsImage(generator = this)
            }
            type.isTable -> {
                onNonTextNode()
                node.parseAsTable(generator = this)
            }
            type.isItalicDeclaration -> {
                onTextNode()
                append(node = node)
            }
            type.isItalic -> {
                onTextNode()
                node.parseAndAnnotateString(
                    annotationMarkerString = node.getAnnotationStartMarkerText(),
                    canAnnotate = { emphasisStart == null },
                    setIndex = { emphasisStart = it },
                    parse = { parseAsItalics(generator = this@IRGenerator, depth = depth + 1) },
                )
            }
            type.isBold -> {
                onTextNode()
                node.parseAndAnnotateString(
                    annotationMarkerString = node.getAnnotationStartMarkerText(),
                    canAnnotate = { strongStart == null },
                    setIndex = { strongStart = it },
                    parse = { parseAsBold(generator = this@IRGenerator, depth = depth + 1) },
                )
            }
            type.isDivider -> {
                onNonTextNode()
                output += MarkdownNode.Divider(raw = getTextString(node = node))
            }
            type.isText -> {
                onTextNode()
                node.processAsText(ignoreNonDollarSignEscapes = isMathParser)
            }
            type.isTextChar -> {
                onTextNode()
                append(node = node)
            }
            type.isListNumber || type.isListBullet -> {
                // no-op
            }
            else -> {
                println("unhandled Markdown node of type ${node.type.name}!")
            }
        }
        if (!wasEOL) {
            consecutiveEOLs.count = 0
        }
        if (depth == 0) {
            if (consecutiveEOLs.count == 1) {
                builder.append('\n')
            }
            commitTextBuffer()
        }
        return output
    }

    private inline fun onNonTextNode(eol: Boolean = false) {
        commitTextBuffer()
        if (!eol && consecutiveEOLs.count >= 2) {
            output += MarkdownNode.LineBreak(raw = "\n".repeat(n = consecutiveEOLs.count))
            consecutiveEOLs.count = 0
        }
    }

    private inline fun onTextNode() {
        if (consecutiveEOLs.count >= 2) {
            output += MarkdownNode.LineBreak(raw = "\n".repeat(n = consecutiveEOLs.count))
            consecutiveEOLs.count = 0
        }
        if (!isBlockquoteParser && consecutiveEOLs.count == 1 && builder.isNotEmpty()) {
            builder.append('\n')
        }
    }

    private fun ASTNode.processAsText(ignoreNonDollarSignEscapes: Boolean = false) {
        builder.append(
            getText(node = this).run {
                if (ignoreNonDollarSignEscapes) {
                    replace(regex = escapedDollarSignRegex, replacement = escapeReplacement("$"))
                } else {
                    replace(regex = backslashRegex, replacement = "")
                }
            },
        )
    }

    private fun ASTNode.parseAndAnnotateString(
        annotationMarkerString: String,
        canAnnotate: () -> Boolean,
        setIndex: (index: Int?) -> Unit,
        parse: ASTNode.() -> Unit,
    ) {
        var annotation: MarkdownTextAnnotation? = null
        if (canAnnotate()) {
            val start = builder.length
            setIndex(start)
            val newAnnotation = MarkdownTextAnnotation(
                start = start,
                isItalic = emphasisStart != null,
                isBold = strongStart != null,
            )
            annotations += newAnnotation
            annotation = newAnnotation
        }
        val markdownNodeCountBeforeParsing = output.size
        parse()
        if (annotation != null) {
            if (builder.length > annotation.start) {
                annotation.endExclusive = builder.length
            } else {
                // this annotation is invalid and should be discarded
                annotations -= annotation
                // Re-add the characters that made up the start marker annotation e.g. ** for bold
                if (output.size > markdownNodeCountBeforeParsing) {
                    for (i in markdownNodeCountBeforeParsing..output.lastIndex) {
                        val node = output[i]
                        if (node is MarkdownNode.Text) {
                            val newRaw = if (annotation.start > node.raw.length) {
                                "${node.raw}$annotationMarkerString"
                            } else {
                                StringBuilder(node.raw).insert(annotation.start, annotationMarkerString).toString()
                            }
                            val newText = if (annotation.start > node.text.length) {
                                "${node.text}$annotationMarkerString"
                            } else {
                                StringBuilder(node.text).insert(annotation.start, annotationMarkerString).toString()
                            }
                            output[i] = node.copy(
                                raw = newRaw,
                                text = newText,
                            )
                            builder.append(annotationMarkerString)
                            break
                        }
                    }
                }
            }
            setIndex(null)
        }
    }

    internal fun commitTextBuffer() {
        if (
            (isBlockquoteParser && builder.isNotBlank()) ||
            (builder.isNotEmpty() && output.lastOrNull() !is MarkdownNode.Divider)
        ) {
            val text = builder.toString().replace(oldChar = '\n', newChar = '\u2005')
            output += MarkdownNode.Text(
                raw = text,
                text = text,
                annotations = annotations.toDisjointList(),
            )
        }
        builder.clear()
        annotations.clear()
        emphasisStart = null
        strongStart = null
    }

    private fun calculateTrailingSpaceCount(): Int {
        var trailingSpaceCount = 0
        var char: Char
        for(i in builder.indices.reversed()) {
            char = builder[i]
            if (char == ' ') {
                trailingSpaceCount += 1
            } else {
                break
            }
        }
        return trailingSpaceCount
    }

    fun getText(node: ASTNode): CharSequence = node.getTextInNode(allFileText = fullText ?: text)
    fun getTextString(node: ASTNode): String = getText(node = node).toString()
    fun getAndAppendTextOf(node: ASTNode, to: StringBuilder) = to.append(getText(node = node))
    private fun append(node: ASTNode) {
        val text = getText(node = node)
        builder.append(if (isBlockquoteParser) text.replace(regex = greaterThanRegex, replacement = "") else text)
    }

    private fun ASTNode.getAnnotationStartMarkerText(): String {
        val markerBuilder = StringBuilder()
        for (child in children) {
            when {
                child.type.isItalicDeclaration -> markerBuilder.append(getText(node = child))
                else -> break
            }
        }
        return markerBuilder.toString()
    }

    fun createSubParser(
        node: ASTNode,
        isLinkParser: Boolean = false,
        isBlockquoteParser: Boolean = false,
        isMathParser: Boolean = false,
        isListParser: Boolean = false,
        listLevel: Int = 0,
    ): IRGenerator = IRGenerator(
        text = getTextString(node = node),
        output = mutableListOf(),
        fullText = fullText ?: this.text,
        isSubParser = true,
        isLinkParser = isLinkParser,
        isBlockquoteParser = isBlockquoteParser,
        isMathParser = isMathParser,
        isListParser = isListParser,
        listLevel = listLevel,
        consecutiveEOLs = consecutiveEOLs,
        syntaxParser = syntaxParser
    )

    fun createSubParser(
        text: String,
        isLinkParser: Boolean = false,
        isBlockquoteParser: Boolean = false,
        isMathParser: Boolean = false,
        isListParser: Boolean = false,
        listLevel: Int = 0,
    ): IRGenerator = IRGenerator(
        text = text,
        output = mutableListOf(),
        fullText = fullText ?: this.text,
        isSubParser = true,
        isLinkParser = isLinkParser,
        isBlockquoteParser = isBlockquoteParser,
        isMathParser = isMathParser,
        isListParser = isListParser,
        listLevel = listLevel,
        consecutiveEOLs = consecutiveEOLs,
        syntaxParser = syntaxParser,
    )

    data class ConsecutiveEOLWrapper(var count: Int)

    companion object {
        private var emphasisStart: Int? = null
        private var strongStart: Int? = null

        private val backslashRegex = """(?<!\\)\\(?!\\)""".toRegex()
        private val escapedDollarSignRegex = """(?<!\\)\\\$""".toRegex()
        private val greaterThanRegex = """(?<!\\)> ?""".toRegex()
    }
}
