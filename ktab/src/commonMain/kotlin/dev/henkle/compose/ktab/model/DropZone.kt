package dev.henkle.compose.ktab.model

enum class DropZone {
    Center,
    Top,
    Right,
    Bottom,
    Left,
    ;

    val isHorizontal: Boolean get() = this == Left || this == Right
    val isVertical: Boolean get() = this == Top || this == Bottom
}
