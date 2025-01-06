package dev.henkle.markdown.parser.jetbrains

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor

enum class MarkdownFlavor(internal val descriptor: CommonMarkFlavourDescriptor) {
    CommonMark(descriptor = CommonMarkFlavourDescriptor()),
    GitHub(descriptor = GFMFlavourDescriptor()),
    Space(descriptor = SFMFlavourDescriptor()),
}
