package dev.henkle.markdown.parser.jetbrains.ext

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

private val textCharTypes = arrayOf(
    MarkdownTokenTypes.WHITE_SPACE,
    MarkdownTokenTypes.LPAREN,
    MarkdownTokenTypes.RPAREN,
    MarkdownTokenTypes.LBRACKET,
    MarkdownTokenTypes.RBRACKET,
    MarkdownTokenTypes.COLON,
    MarkdownTokenTypes.EXCLAMATION_MARK,
    MarkdownTokenTypes.BACKTICK,
    MarkdownTokenTypes.ESCAPED_BACKTICKS,
    MarkdownTokenTypes.SINGLE_QUOTE,
    MarkdownTokenTypes.DOUBLE_QUOTE,
    GFMTokenTypes.DOLLAR,
    GFMTokenTypes.TILDE,
)

private val headerTypes = arrayOf(
    MarkdownElementTypes.ATX_1,
    MarkdownElementTypes.ATX_2,
    MarkdownElementTypes.ATX_3,
    MarkdownElementTypes.ATX_4,
    MarkdownElementTypes.ATX_5,
    MarkdownElementTypes.ATX_6,
)

private val lineHeaderTypes = arrayOf(
    MarkdownElementTypes.SETEXT_1,
    MarkdownElementTypes.SETEXT_2,
)

val IElementType.isText: Boolean get() = this == MarkdownTokenTypes.TEXT
val IElementType.isCodeLine: Boolean get() = this == MarkdownTokenTypes.CODE_LINE
val IElementType.isBlockQuoteStart: Boolean get() = this == MarkdownTokenTypes.BLOCK_QUOTE
val IElementType.isHtmlBlockContent: Boolean get() = this == MarkdownTokenTypes.HTML_BLOCK_CONTENT
val IElementType.isHtmlTag: Boolean get() = this == MarkdownTokenTypes.HTML_TAG
val IElementType.isSingleQuote: Boolean get() = this == MarkdownTokenTypes.SINGLE_QUOTE
val IElementType.isDoubleQuote: Boolean get() = this == MarkdownTokenTypes.DOUBLE_QUOTE
val IElementType.isLParenthesis: Boolean get() = this == MarkdownTokenTypes.LPAREN
val IElementType.isRParenthesis: Boolean get() = this == MarkdownTokenTypes.RPAREN
val IElementType.isLBracket: Boolean get() = this == MarkdownTokenTypes.LBRACKET
val IElementType.isRBracket: Boolean get() = this == MarkdownTokenTypes.RBRACKET
val IElementType.isLAngleBracket: Boolean get() = this == MarkdownTokenTypes.LT
val IElementType.isRAngleBracket: Boolean get() = this == MarkdownTokenTypes.GT
val IElementType.isColon: Boolean get() = this == MarkdownTokenTypes.COLON
val IElementType.isExclamation: Boolean get() = this == MarkdownTokenTypes.EXCLAMATION_MARK
val IElementType.isBacktick: Boolean get() = this == MarkdownTokenTypes.BACKTICK
val IElementType.isEscapedBacktick: Boolean get() = this == MarkdownTokenTypes.ESCAPED_BACKTICKS
val IElementType.isBreak: Boolean get() = this == MarkdownTokenTypes.HARD_LINE_BREAK
val IElementType.isEOL: Boolean get() = this == MarkdownTokenTypes.EOL
val IElementType.isLinkID: Boolean get() = this == MarkdownTokenTypes.LINK_ID
val IElementType.isHeaderDeclaration: Boolean get() = this == MarkdownTokenTypes.ATX_HEADER
val IElementType.isHeaderContent: Boolean get() = this == MarkdownTokenTypes.ATX_CONTENT
val IElementType.isEqualsLineHeaderDeclaration: Boolean get() = this == MarkdownTokenTypes.SETEXT_1
val IElementType.isDashLineHeaderDeclaration: Boolean get() = this == MarkdownTokenTypes.SETEXT_2
val IElementType.isLineHeaderContent: Boolean get() = this == MarkdownTokenTypes.SETEXT_CONTENT
val IElementType.isItalicDeclaration: Boolean get() = this == MarkdownTokenTypes.EMPH
val IElementType.isListBullet: Boolean get() = this == MarkdownTokenTypes.LIST_BULLET
val IElementType.isListNumber: Boolean get() = this == MarkdownTokenTypes.LIST_NUMBER
val IElementType.isUrl: Boolean get() = this == MarkdownTokenTypes.URL
val IElementType.isDivider: Boolean get() = this == MarkdownTokenTypes.HORIZONTAL_RULE
val IElementType.isCodeFenceLanguage: Boolean get() = this == MarkdownTokenTypes.FENCE_LANG
val IElementType.isCodeFenceStart: Boolean get() = this == MarkdownTokenTypes.CODE_FENCE_START
val IElementType.isCodeFenceEnd: Boolean get() = this == MarkdownTokenTypes.CODE_FENCE_END
val IElementType.isLinkTitleDeclaration: Boolean get() = this == MarkdownTokenTypes.LINK_TITLE
val IElementType.isAutolinkDeclaration: Boolean get() = this == MarkdownTokenTypes.AUTOLINK
val IElementType.isEmailAutolink: Boolean get() = this == MarkdownTokenTypes.EMAIL_AUTOLINK
val IElementType.isBadCharacter: Boolean get() = this == MarkdownTokenTypes.BAD_CHARACTER
val IElementType.isWhitespace: Boolean get() = this == MarkdownTokenTypes.WHITE_SPACE

val IElementType.isFile: Boolean get() = this == MarkdownElementTypes.MARKDOWN_FILE
val IElementType.isUnorderedList: Boolean get() = this == MarkdownElementTypes.UNORDERED_LIST
val IElementType.isOrderedList: Boolean get() = this == MarkdownElementTypes.ORDERED_LIST
val IElementType.isListItem: Boolean get() = this == MarkdownElementTypes.LIST_ITEM
val IElementType.isBlockQuote: Boolean get() = this == MarkdownElementTypes.BLOCK_QUOTE
val IElementType.isCodeFence: Boolean get() = this == MarkdownElementTypes.CODE_FENCE
val IElementType.isCodeBlock: Boolean get() = this == MarkdownElementTypes.CODE_BLOCK
val IElementType.isCodeSpan: Boolean get() = this == MarkdownElementTypes.CODE_SPAN
val IElementType.isHtmlBlock: Boolean get() = this == MarkdownElementTypes.HTML_BLOCK
val IElementType.isParagraph: Boolean get() = this == MarkdownElementTypes.PARAGRAPH
val IElementType.isItalic: Boolean get() = this == MarkdownElementTypes.EMPH
val IElementType.isBold: Boolean get() = this == MarkdownElementTypes.STRONG
val IElementType.isLinkDefinition: Boolean get() = this == MarkdownElementTypes.LINK_DEFINITION
val IElementType.isLinkLabel: Boolean get() = this == MarkdownElementTypes.LINK_LABEL
val IElementType.isLinkDestination: Boolean get() = this == MarkdownElementTypes.LINK_DESTINATION
val IElementType.isLinkTitle: Boolean get() = this == MarkdownElementTypes.LINK_TITLE
val IElementType.isLinkText: Boolean get() = this == MarkdownElementTypes.LINK_TEXT
val IElementType.isInlineLink: Boolean get() = this == MarkdownElementTypes.INLINE_LINK
val IElementType.isFullReferenceLink: Boolean get() = this == MarkdownElementTypes.FULL_REFERENCE_LINK
val IElementType.isShortReferenceLink: Boolean get() = this == MarkdownElementTypes.SHORT_REFERENCE_LINK
val IElementType.isImage: Boolean get() = this == MarkdownElementTypes.IMAGE
val IElementType.isAutolink: Boolean get() = this == MarkdownElementTypes.AUTOLINK
val IElementType.isEqualsLineHeader: Boolean get() = this == MarkdownElementTypes.SETEXT_1
val IElementType.isDashLineHeader: Boolean get() = this == MarkdownElementTypes.SETEXT_2
val IElementType.isH1: Boolean get() = this == MarkdownElementTypes.ATX_1
val IElementType.isH2: Boolean get() = this == MarkdownElementTypes.ATX_2
val IElementType.isH3: Boolean get() = this == MarkdownElementTypes.ATX_3
val IElementType.isH4: Boolean get() = this == MarkdownElementTypes.ATX_4
val IElementType.isH5: Boolean get() = this == MarkdownElementTypes.ATX_5
val IElementType.isH6: Boolean get() = this == MarkdownElementTypes.ATX_6

val IElementType.isDollar: Boolean get() = this == GFMTokenTypes.DOLLAR
val IElementType.isTilde: Boolean get() = this == GFMTokenTypes.TILDE
val IElementType.isTableCell: Boolean get() = this == GFMTokenTypes.CELL
val IElementType.isGFMAutolink: Boolean get() = this == GFMTokenTypes.GFM_AUTOLINK
val IElementType.isCheckBox: Boolean get() = this == GFMTokenTypes.CHECK_BOX
val IElementType.isTableSeparator: Boolean get() = this == GFMTokenTypes.TABLE_SEPARATOR

val IElementType.isInlineMath: Boolean get() = this == GFMElementTypes.INLINE_MATH
val IElementType.isMathBlock: Boolean get() = this == GFMElementTypes.BLOCK_MATH
val IElementType.isTable: Boolean get() = this == GFMElementTypes.TABLE
val IElementType.isTableRow: Boolean get() = this == GFMElementTypes.ROW
val IElementType.isTableHeader: Boolean get() = this == GFMElementTypes.HEADER
val IElementType.isStrikethrough: Boolean get() = this == GFMElementTypes.STRIKETHROUGH

val IElementType.isTextChar: Boolean get() = this in textCharTypes
val IElementType.isHeader: Boolean get() = this in headerTypes
val IElementType.isLineHeader: Boolean get() = this in lineHeaderTypes
