package dev.henkle.markdown.ui.demo

import platform.CoreGraphics.CGFloat
import platform.UIKit.UIColor
import platform.UIKit.UIView

interface LatexWrapperFactory {
    fun create(
        text: String,
        fontSizePt: CGFloat = 12.0,
        textColor: UIColor = UIColor.blackColor,
        textAlignment: IOSTextAlignment = IOSTextAlignment.Left,
        onSizeChanged: (widthPt: CGFloat, heightPt: CGFloat) -> Unit,
    ): LatexWrapper
}

interface LatexWrapper {
    val view: UIView
    fun update(
        text: String,
        fontSizePt: CGFloat = 12.0,
        textColor: UIColor = UIColor.blackColor,
        textAlignment: IOSTextAlignment = IOSTextAlignment.Left,
    )
}
