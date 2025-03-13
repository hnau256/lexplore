package hnau.lexplore.common.ui.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface TableScope {

    val orientation: TableOrientation

    fun Modifier.weight(
        weight: Float,
    ): Modifier

    fun cell(
        content: @Composable (TableCorners) -> Unit,
    )
}