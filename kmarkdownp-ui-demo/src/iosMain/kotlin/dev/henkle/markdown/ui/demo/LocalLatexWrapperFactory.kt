package dev.henkle.markdown.ui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalLatexWrapperFactory = compositionLocalOf<LatexWrapperFactory> {
    error("No LatexWrapperFactory was provided to LocalLatexWrapperFactory!")
}

@Composable
internal fun ProvideLatexWrapperFactory(factory: LatexWrapperFactory, content: @Composable () -> Unit) = CompositionLocalProvider(
    value = LocalLatexWrapperFactory provides factory,
    content = content,
)
