package hnau.lexplore.common.ui.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import hnau.lexplore.common.ui.uikit.table.layout.LayoutWeightElement
import hnau.lexplore.common.ui.uikit.table.layout.TableLayout
import hnau.lexplore.common.ui.uikit.utils.Dimens

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

        override fun cell(
            content: @Composable (TableCorners) -> Unit,
        ) {
            cells += content
        }
    }

    tableScope.content()

    TableLayout(
        orientation = orientation,
        modifier = modifier,
        separationPx = with(LocalDensity.current) { Dimens.chipsSeparation.roundToPx() },
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