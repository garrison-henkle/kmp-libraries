package dev.henkle.markdown.ui.utils.ext

import dev.henkle.markdown.ui.model.UIElement

fun List<UIElement>.getText(): String = StringBuilder().apply {
    for (element in this@getText) {
        if (element is UIElement.Text) {
            append(element.text)
        }
    }
}.toString()
