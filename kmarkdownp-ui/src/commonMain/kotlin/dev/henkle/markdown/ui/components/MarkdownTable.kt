package dev.henkle.markdown.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.henkle.markdown.ui.MarkdownContent
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle
import dev.henkle.markdown.ui.utils.ProvideMarkdownStyle
import eu.wewox.lazytable.LazyTable
import eu.wewox.lazytable.LazyTableItem
import eu.wewox.lazytable.lazyTableDimensions
import eu.wewox.lazytable.rememberLazyTableState

private data class MarkdownTableCellInfo(
    val cell: UIElement.Table.Cell,
    val x: Int,
    val y: Int,
)

@Composable
fun MarkdownTable(modifier: Modifier = Modifier, element: UIElement.Table) {
    val markdownStyle = LocalMarkdownStyle.current
    val style = markdownStyle.table
    val state = rememberLazyTableState()

    val cellInfo = remember(element) {
        element.cells.flatMapIndexed { y, row ->
            row.mapIndexed { x, cell ->
                MarkdownTableCellInfo(cell = cell, x = x, y = y)
            }
        }
    }

    var rowHeights by remember(element) { mutableStateOf<List<Dp>?>(null) }
    var columnWidths by remember(element) { mutableStateOf<List<Dp>?>(null) }

    ProvideMarkdownStyle(
        style = markdownStyle.copy(
            text = style.textStyle,
            image = style.imageStyle,
        ),
    ) {
        SubcomposeLayout(modifier = modifier) { constraints ->
            val (widths, heights) = rowHeights?.let { heights ->
                columnWidths?.let { widths ->
                    widths to heights
                }
            } ?: run {
                val cellMeasurements = MutableList<MutableList<DpSize>>(size = element.cells.size) { mutableListOf() }
                val heights = MutableList(size = element.cells.size) { 0.dp }
                val widths = MutableList(size = element.cells.firstOrNull()?.size ?: 0) { 0.dp }
                var width: Dp
                var height: Dp
                element.cells.forEachIndexed { y, row ->
                    row.forEachIndexed { x, cell ->
                        val measurements = subcompose(slotId = "cellDimensionsMeasurement-$x-$y)") {
                            MarkdownContent(elements = cell.content)
                        }[0].measure(Constraints())
                        val padding = style.cellMeasurementPadding(x, y)
                        width = measurements.width.toDp() + padding.start + padding.end
                        height = measurements.height.toDp() + padding.top + padding.bottom
                        if (widths[x] < width) {
                            widths[x] = width
                        }
                        if (heights[y] < height) {
                            heights[y] = height
                        }
                        cellMeasurements[x] += DpSize(width = width, height = height)
                    }
                }
                columnWidths = widths
                rowHeights = heights
                widths to heights
            }
            val tablePlaceable = subcompose(slotId = "table") {
                LazyTable(
                    modifier = modifier,
                    state = state,
                    dimensions = lazyTableDimensions(columnsSize = widths, rowsSize = heights)
                ) {
                    items(
                        items = cellInfo,
                        layoutInfo = { cell -> LazyTableItem(row = cell.y, column = cell.x) },
                    ) { (cell, x, y) ->
                        val cellModifier = remember(cell, x, y) {
                            style.cellModifier(x, y, widths.size, heights.size, element.hasHeader)
                        }
                        MarkdownContent(
                            modifier = cellModifier,
                            elements = cell.content,
                        )
                    }
                }
            }[0].measure(constraints = constraints)

            layout(width = tablePlaceable.width, height = tablePlaceable.height) {
                tablePlaceable.place(x = 0, y = 0)
            }
        }
    }
}
