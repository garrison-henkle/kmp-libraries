import androidx.compose.ui.window.ComposeUIViewController
import dev.henkle.stytch.demo.App
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    return ComposeUIViewController { App() }
}
