//
//  LatexView.swift
//  iosApp
//
//  Created by Garrison Henkle on 8/24/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import KMarkdownP
import SwiftMath
import UIKit
import SwiftUI

class IOSLatextWrapperFactory : NSObject, LatexWrapperFactory {
    func create(
        text: String,
        fontSizePt: Double,
        textColor: UIColor,
        textAlignment: IOSTextAlignment,
        onSizeChanged: @escaping (KotlinDouble, KotlinDouble) -> Void
    ) -> LatexWrapper {
        LatexWrapperImpl(
            text: text,
            fontSizePt: fontSizePt,
            textColor: textColor,
            textAlignment: textAlignment,
            sizeDidChange: { widthPt, heightPt in
                onSizeChanged(
                    KotlinDouble(value: widthPt),
                    KotlinDouble(value: heightPt)
                )
            }
        )
    }
}

class LatexWrapperViewController : UIViewController {
    private var text: String
    private var fontSize: CGFloat
    private var textColor: UIColor
    private var textAlignment: IOSTextAlignment
    private var sizeDidChange: (CGFloat, CGFloat) -> Void

    private var latexView: MTMathUILabel!

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    init(
        text: String,
        fontSize: CGFloat,
        textColor: UIColor,
        textAlignment: IOSTextAlignment,
        sizeDidChange: @escaping (CGFloat, CGFloat) -> Void
    ) {
        self.text = text
        self.fontSize = fontSize
        self.textColor = textColor
        self.textAlignment = textAlignment
        self.sizeDidChange = sizeDidChange
        super.init(nibName: nil, bundle: nil)
    }

    override func loadView() {
        latexView = MTMathUILabel()
        view = latexView
        view.translatesAutoresizingMaskIntoConstraints = false
        latexView.latex = text
        latexView.fontSize = fontSize
        latexView.textColor = textColor
        latexView.textAlignment = textAlignment.asMTTextAlignment()
    }

    override func viewDidAppear(_ animated: Bool) {
        sizeDidChange(view.frame.size.width, view.frame.size.height)
    }
    
    override func viewDidLayoutSubviews() {
        sizeDidChange(view.frame.size.width, view.frame.size.height)
    }

    func update(
        text: String,
        fontSize: CGFloat,
        textColor: UIColor,
        textAlignment: IOSTextAlignment
    ) {
        self.text = text
        self.fontSize = fontSize
        self.textColor = textColor
        self.textAlignment = textAlignment
        latexView.latex = text
        latexView.fontSize = fontSize
        latexView.textColor = textColor
        latexView.textAlignment = textAlignment.asMTTextAlignment()
    }
}

class LatexWrapperImpl : NSObject, LatexWrapper {
    let view: UIView
    let controller: LatexWrapperViewController
    init(
        text: String,
        fontSizePt: CGFloat,
        textColor: UIColor,
        textAlignment: IOSTextAlignment,
        sizeDidChange: @escaping (CGFloat, CGFloat) -> Void
    ) {
        self.controller = LatexWrapperViewController(text: text, fontSize: fontSizePt, textColor: textColor, textAlignment: textAlignment, sizeDidChange: sizeDidChange)
        self.view = controller.view
    }
    
    func update(
        text: String,
        fontSizePt: Double,
        textColor: UIColor,
        textAlignment: IOSTextAlignment
    ) {
        self.controller.update(text: text, fontSize: fontSizePt, textColor: textColor, textAlignment: textAlignment)
    }
}

private extension IOSTextAlignment {
    func asMTTextAlignment() -> MTTextAlignment {
        switch(self) {
        case .left:
            .left
        case .center:
            .center
        case .right:
            .right
        default:
            // this branch should never run
            .left
        }
    }
}
