package hnau.lexplore.common.ui.uikit.table

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

fun TableScope.subtable(
    modifier: Modifier = Modifier,
    content: TableScope.() -> Unit,
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

fun TableScope.cellBox(
    modifier: Modifier = Modifier,
    backgroundColor: @Composable () -> Color = { MaterialTheme.colorScheme.surfaceContainer },
    contentAlignment: Alignment = Alignment.Center,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.(Shape) -> Unit,
) {
    cell { corners ->
        val shape = corners.toShape()
        Box(
            modifier = modifier.background(
                color = backgroundColor(),
                shape = shape
            ),
            contentAlignment = contentAlignment,
            propagateMinConstraints = propagateMinConstraints,
        ) {
            content(shape)
        }
    }
}