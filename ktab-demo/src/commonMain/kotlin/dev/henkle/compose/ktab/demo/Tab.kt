package dev.henkle.compose.ktab.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.henkle.compose.ktab.model.Tab as KTab
import dev.henkle.compose.ktab.model.TabCompanion
import dev.henkle.compose.ktab.model.TabCompanion.DeserializeResult
import dev.henkle.compose.ktab.model.TabId
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.fetchAndIncrement

internal data class Tab(
    override val id: TabId = TabId(id = nextId.fetchAndIncrement().toString()),
    override val title: String = "Tab ${id.id}",
) : KTab<Tab> {
    override val companion = Companion

    override fun serialize(): String = id.id

    @Composable
    override fun Content() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colors[id.id.toInt() % colors.size]),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = if (id.id.toInt() % colors.size > 3) {
                    Color.White
                } else {
                    Color.Black
                },
            )
        }
    }

    companion object : TabCompanion<Tab> {
        private val nextId = AtomicInt(value = 0)
        private val colors = listOf(
            Color.Cyan,
            Color.Yellow,
            Color.Magenta,
            Color.LightGray,
            Color.Red,
            Color.Blue,
            Color.Green,
        )

        override fun deserialize(string: String): DeserializeResult<Tab> =
            try {
                string.toInt()
                DeserializeResult.Ok(
                    tab = Tab(
                        id = TabId(id = string),
                        title = "Tab $string",
                    )
                )
            } catch (ex: NumberFormatException) {
                DeserializeResult.Error(ex = ex)
            }
    }
}
