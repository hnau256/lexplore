package hnau.lexplore.common.ui.uikit.table.layout

import androidx.compose.ui.layout.IntrinsicMeasurable

data class TableParentData(
    var weight: Float? = null,
)

val IntrinsicMeasurable.tableParentData: TableParentData?
    get() = parentData as? TableParentData

