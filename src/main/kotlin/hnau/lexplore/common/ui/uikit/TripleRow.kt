package hnau.lexplore.common.ui.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.offset

@Composable
fun TripleRow(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment.Horizontal,
    leading: @Composable () -> Unit,
    content: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
) = Layout(
    content = {
        leading()
        content()
        trailing()
    },
    modifier = modifier,
) { measurables, constraints ->

    val (leadingMeasurable, contentMeasurable, trailingMeasurable) = measurables
        .takeIf { it.size == 3 }
        ?: error("TripleRow expects 3 items (got ${measurables.size})")

    val measurablesConstraints = constraints.copy(minHeight = 0, minWidth = 0)

    val leadingPlaceable = leadingMeasurable.measure(measurablesConstraints)

    val constraintsWithoutLeading = measurablesConstraints.offset(
        horizontal = -leadingPlaceable.width,
    )
    val trailingPlaceable = trailingMeasurable.measure(constraintsWithoutLeading)

    val contentConstraints = constraintsWithoutLeading.offset(
        horizontal = -trailingPlaceable.width,
    )
    val contentPlaceable = contentMeasurable.measure(contentConstraints)

    val width = constraints.constrainWidth(
        leadingPlaceable.width +
                contentPlaceable.width +
                trailingPlaceable.width,
    )
    val height = constraints.constrainHeight(
        maxOf(leadingPlaceable.height, contentPlaceable.height, trailingPlaceable.height),
    )

    layout(width, height) {
        leadingPlaceable.placeRelative(
            x = 0,
            y = (height - leadingPlaceable.height) / 2,
        )
        val contentOffset = contentAlignment.align(
            layoutDirection = layoutDirection,
            size = contentPlaceable.width,
            space = width - leadingPlaceable.width - trailingPlaceable.width,
        )
        contentPlaceable.placeRelative(
            x = leadingPlaceable.width + contentOffset,
            y = (height - contentPlaceable.height) / 2,
        )
        trailingPlaceable.placeRelative(
            x = width - trailingPlaceable.width,
            y = (height - trailingPlaceable.height) / 2,
        )
    }
}
