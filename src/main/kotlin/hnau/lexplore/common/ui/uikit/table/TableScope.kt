package hnau.lexplore.common.ui.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface TableScope {

    val orientation: TableOrientation

    @Composable
    fun Cell(
        content: @Composable (TableCorners) -> Unit,
    )
}