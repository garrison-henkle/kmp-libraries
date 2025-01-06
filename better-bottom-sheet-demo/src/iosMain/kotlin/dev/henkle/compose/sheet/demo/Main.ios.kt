package dev.henkle.compose.sheet.demo

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("unused", "FunctionName")
fun MainViewController(): UIViewController =
    ComposeUIViewController(
        configure = { onFocusBehavior = OnFocusBehavior.DoNothing },
        content = { App() },
    )
