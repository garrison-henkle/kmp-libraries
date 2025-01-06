package dev.henkle.markdown.parser.treesitter.utils

val String.isInline: Boolean get() = this == "inline" || this == "pipe_table_cell"
