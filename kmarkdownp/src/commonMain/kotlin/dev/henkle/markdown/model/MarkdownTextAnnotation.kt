package dev.henkle.markdown.model

// TODO(garrison): make this into an interface so endExclusive isn't accessible to the consumer
class MarkdownTextAnnotation(
    val start: Int,
    endExclusive: Int = start,
    val isItalic: Boolean = false,
    val isBold: Boolean = false,
) {
    var endExclusive: Int = endExclusive

    fun copy(
        start: Int = this.start,
        endExclusive: Int = this.endExclusive,
        isStrong: Boolean = this.isBold,
        isItalic: Boolean = this.isItalic,
    ) = MarkdownTextAnnotation(start = start, endExclusive = endExclusive, isItalic = isItalic, isBold = isStrong)

    override fun toString(): String =
        StringBuilder("MarkdownTextAnnotation(start=$start, endExclusive=$endExclusive, style=[")
            .apply {
                if (isItalic) append("italic")
                if (isItalic && isBold) append(", ")
                if (isBold) append("bold")
                append("])")
            }.toString()
}
