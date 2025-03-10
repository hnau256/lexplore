package hnau.lexplore.common.ui.uikit.table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

        @Composable
        override fun Cell(
            content: @Composable (TableCorners) -> Unit,
        ) {
            cells += content
        }
    }

    tableScope.content()

    val cellsContent = @Composable {
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

    val arrangement = Arrangement.spacedBy(Dimens.chipsSeparation)
    when (orientation) {
        TableOrientation.Vertical -> Column(
            modifier = modifier,
            verticalArrangement = arrangement,
        ) { cellsContent() }

        TableOrientation.Horizontal -> Row(
            modifier = modifier,
            horizontalArrangement = arrangement,
        ) { cellsContent() }
    }
}