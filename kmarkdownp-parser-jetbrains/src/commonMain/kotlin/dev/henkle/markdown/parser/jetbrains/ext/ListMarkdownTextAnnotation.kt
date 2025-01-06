package dev.henkle.markdown.parser.jetbrains.ext

import dev.henkle.markdown.model.MarkdownTextAnnotation

// this creates a new list so the annotations list can be cleared and re-used
internal fun List<MarkdownTextAnnotation>.toDisjointList(): List<MarkdownTextAnnotation> {
    if (size <= 1) return toList()

    val disjointAnnotations = mutableListOf<MarkdownTextAnnotation>()
    var lastAnnotation = first()
    var currentAnnotation: MarkdownTextAnnotation? = null
    for (i in 1..<size) {
        currentAnnotation = get(i)
        if (
            !(currentAnnotation.start < lastAnnotation.endExclusive &&
            currentAnnotation.start > lastAnnotation.start)
        ) {
            disjointAnnotations += lastAnnotation
            lastAnnotation = currentAnnotation
        } else if (
            currentAnnotation.isBold != lastAnnotation.isBold ||
            currentAnnotation.isItalic != lastAnnotation.isItalic
        ){
            disjointAnnotations += lastAnnotation.copy(endExclusive = currentAnnotation.start)
            lastAnnotation = currentAnnotation
        }
    }
    currentAnnotation?.also { disjointAnnotations += it }
    return disjointAnnotations
}
