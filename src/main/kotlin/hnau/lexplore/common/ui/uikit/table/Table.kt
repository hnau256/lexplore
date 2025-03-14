package hnau.lexplore.common.ui.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import hnau.lexplore.common.ui.uikit.table.layout.TableLayout
import hnau.lexplore.common.ui.uikit.utils.Dimens

@Composable
fun Table(
    orientation: TableOrientation,
    modifier: Modifier = Modifier,
    corners: TableCorners = TableCorners.opened,
    content: TableScope.() -> Unit,
) {
    val cells: List<@Composable (TableCorners) -> Unit> = remember(orientation, content) {
        val scope = TableScopeImpl(
            orientation = orientation,
        )
        scope.content()
        scope.cells
    }
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