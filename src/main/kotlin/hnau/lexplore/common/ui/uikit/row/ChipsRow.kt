package hnau.lexplore.common.ui.uikit.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.lexplore.common.ui.uikit.shape.HnauShape
import hnau.lexplore.common.ui.uikit.shape.create
import hnau.lexplore.common.ui.uikit.shape.inRow
import hnau.lexplore.common.ui.uikit.utils.Dimens

@Composable
fun <T> ChipsRow(
    all: List<T>,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable (item: T, shape: Shape) -> Unit,
) = Row(
    modifier = modifier,
    verticalAlignment = verticalAlignment,
    horizontalArrangement = Arrangement.spacedBy(
        space = Dimens.chipsSeparation,
        alignment = horizontalAlignment,
    ),
) {
    val totalCount = all.size
    all.forEachIndexed { i, item ->
        val shape = HnauShape.inRow.create(
            totalCount = totalCount,
            index = i,
        )
        content(item, shape)
    }
}
