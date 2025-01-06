package dev.henkle.markdown.ui.model

import androidx.compose.runtime.Composable

typealias UIComponent<T> = @Composable (element: T) -> Unit
