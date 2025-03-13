package hnau.lexplore.common.ui.uikit.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TableScope.Table(
    modifier: Modifier = Modifier,
    content: @Composable TableScope.() -> Unit,
) {
    cell { corners ->
        Table(
            modifier = modifier,
            orientation = orientation.opposite,
            content = content,
            corners = corners,
        )
    }
}