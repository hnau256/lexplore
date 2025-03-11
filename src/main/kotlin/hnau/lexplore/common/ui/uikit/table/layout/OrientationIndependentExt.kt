package hnau.lexplore.common.ui.uikit.table.layout

import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import hnau.lexplore.common.ui.uikit.table.TableOrientation

fun Constraints.maxAlong(
    orientation: TableOrientation,
): Int? = orientation.fold(
    ifVertical = { maxHeight.takeIf { hasBoundedHeight } },
    ifHorizontal = { maxWidth.takeIf { hasBoundedWidth } },
)

fun Constraints.maxAcross(
    orientation: TableOrientation,
): Int? = maxAlong(
    orientation = orientation.opposite,
)

fun Constraints.minAlong(
    orientation: TableOrientation,
): Int? = orientation.fold(
    ifVertical = { minHeight.takeIf { it > 0 } },
    ifHorizontal = { minWidth.takeIf { it > 0 } },
)

fun Constraints.minAcross(
    orientation: TableOrientation,
): Int? = minAlong(
    orientation = orientation.opposite,
)

fun Placeable.along(
    orientation: TableOrientation,
): Int = orientation.fold(
    ifVertical = { height },
    ifHorizontal = { width },
)

fun Placeable.across(
    orientation: TableOrientation,
): Int = along(
    orientation = orientation.opposite,
)

fun MeasureScope.layout(
    orientation: TableOrientation,
    along: Int,
    across: Int,
    alignmentLines: Map<AlignmentLine, Int> = emptyMap(),
    placementBlock: Placeable.PlacementScope.() -> Unit,
): MeasureResult = layout(
    width = orientation.fold(
        ifVertical = { across },
        ifHorizontal = { along },
    ),
    height = orientation.fold(
        ifVertical = { along },
        ifHorizontal = { across },
    ),
    alignmentLines = alignmentLines,
    placementBlock = placementBlock
)

fun Constraints.reduceAlong(
    orientation: TableOrientation,
    value: Int,
): Constraints = orientation.fold(
    ifVertical = {
        val minHeight = (minHeight - value).coerceAtLeast(0)
        copy(
            minHeight = minHeight,
            maxHeight = (maxHeight - value).coerceAtLeast(minHeight),
        )
    },
    ifHorizontal = {
        val minWidth = (minWidth - value).coerceAtLeast(0)
        copy(
            minWidth = minWidth,
            maxWidth = (maxWidth - value).coerceAtLeast(minWidth),
        )
    }
)

fun Constraints.constrainAlong(
    orientation: TableOrientation,
    value: Int,
): Int = orientation.fold(
    ifVertical = { constrainHeight(value) },
    ifHorizontal = { constrainWidth(value) },
)

fun Constraints.constrainAcross(
    orientation: TableOrientation,
    value: Int,
): Int = constrainAlong(
    orientation = orientation.opposite,
    value = value,
)

fun Constraints.Companion.fixed(
    orientation: TableOrientation,
    along: Int,
    across: Int,
): Constraints = orientation.fold(
    ifVertical = {
        Constraints.fixed(
            width = across,
            height = along,
        )
    },
    ifHorizontal = {
        Constraints.fixed(
            width = along,
            height = across,
        )
    }
)

fun Placeable.placeRelative(
    scope: Placeable.PlacementScope,
    orientation: TableOrientation,
    along: Int,
    across: Int,
) {
    with(scope) {
        orientation.fold(
            ifVertical = {
                placeRelative(
                    x = across,
                    y = along,
                )
            },
            ifHorizontal = {
                placeRelative(
                    x = along,
                    y = across,
                )
            }
        )
    }
}