package hnau.lexplore.common.ui.uikit.table

import androidx.compose.runtime.Composable

@Composable
fun TableScope.Table(
    content: @Composable TableScope.() -> Unit,
) {
    Cell { corners ->
        Table(
            orientation = orientation.opposite,
            content = content,
            corners = corners,
        )
    }
}