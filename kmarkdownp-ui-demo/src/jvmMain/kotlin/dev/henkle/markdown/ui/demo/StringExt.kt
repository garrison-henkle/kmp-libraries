package dev.henkle.markdown.ui.demo

val String.isInline: Boolean get() = this == "inline" || this == "pipe_table_cell"
