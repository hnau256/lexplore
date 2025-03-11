package hnau.lexplore.common.ui.uikit.table.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.common.ui.uikit.table.TableOrientation
import kotlin.math.max

@Composable
fun TableLayout(
    orientation: TableOrientation,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->

        var maxAcross = 0
        var alongSum = 0
        var weightSum: Float? = null

        val alongs = measurables.map { measurable ->
            val placeable = measurable
                .measure(
                    constraints = constraints.reduceAlong(
                        orientation = orientation,
                        value = alongSum
                    )
                )
            val along = placeable.along(
                orientation = orientation,
            )
            val across = placeable.across(
                orientation = orientation,
            )
            maxAcross = max(maxAcross, across)
            alongSum += along
            measurable
                .tableParentData
                ?.weight
                ?.let { weight ->
                    weightSum = (weightSum ?: 0f) + weight
                }
            along
        }

        val along = constraints
            .maxAlong(orientation)
            .ifNull { alongSum }
            .let { along ->
                constraints.constrainAlong(
                    orientation = orientation,
                    value = along,
                )
            }

        val alongToSplitByWeights = along - alongSum

        val across = constraints.constrainAcross(
            orientation = orientation,
            value = maxAcross,
        )

        val placeables = measurables.mapIndexed { index, measurable ->
            val weightAlong = weightSum?.let { sum ->
                measurable.tableParentData?.weight?.let { weight ->
                    (alongToSplitByWeights * weight / sum).toInt()
                }
            } ?: 0
            measurable.measure(
                Constraints.fixed(
                    orientation = orientation,
                    along = alongs[index] + weightAlong, //TODO calc last as difference
                    across = across,
                )
            )
        }

        layout(
            orientation = orientation,
            along = along,
            across = across,
        ) {
            var currentAlong = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(
                    scope = this,
                    orientation = orientation,
                    along = currentAlong,
                    across = 0,
                )
                currentAlong += placeable.along(
                    orientation = orientation,
                )
            }
        }
    }
}