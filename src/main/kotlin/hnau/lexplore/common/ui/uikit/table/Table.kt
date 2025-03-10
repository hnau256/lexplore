package hnau.lexplore.common.ui.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.lexplore.common.ui.uikit.table.layout.LayoutWeightElement
import hnau.lexplore.common.ui.uikit.table.layout.TableLayout

@Composable
fun Table(
    orientation: TableOrientation,
    modifier: Modifier = Modifier,
    corners: TableCorners = TableCorners.opened,
    content: @Composable TableScope.() -> Unit,
) {
    val cells: MutableList<@Composable (TableCorners) -> Unit> = mutableListOf()

    val tableScope = object : TableScope {

        override val orientation: TableOrientation
            get() = orientation

        override fun Modifier.weight(
            weight: Float,
        ): Modifier = then(
            LayoutWeightElement(
                weight = weight,
            )
        )

        @Composable
        override fun Cell(
            content: @Composable (TableCorners) -> Unit,
        ) {
            cells += content
        }
    }

    tableScope.content()

    TableLayout(
        orientation = orientation,
        modifier = modifier,
    ) {
        cells.forEachIndexed { i, cell ->
            cell(
                corners.close(
                    orientation = orientation,
                    startOrTop = i > 0,
                    endOrBottom = i < cells.lastIndex,
                )
            )
        }
    }
}