import androidx.compose.ui.window.ComposeUIViewController
import dev.henkle.markdown.ui.demo.App
import dev.henkle.markdown.ui.demo.LatexWrapperFactory
import dev.henkle.markdown.ui.demo.ProvideLatexWrapperFactory
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun MainViewController(latexWrapperFactory: LatexWrapperFactory): UIViewController =
    ComposeUIViewController {
        ProvideLatexWrapperFactory(factory = latexWrapperFactory) {
            App()
        }
    }
