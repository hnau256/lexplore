package hnau.lexplore.common.ui.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.lexplore.common.ui.uikit.table.layout.LayoutWeightElement

class TableScopeImpl(
    override val orientation: TableOrientation,
) : TableScope {

    private val _cells: MutableList<@Composable (TableCorners) -> Unit> = mutableListOf()

    val cells: List<@Composable (TableCorners) -> Unit>
        get() = _cells

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
        _cells += content
    }
}