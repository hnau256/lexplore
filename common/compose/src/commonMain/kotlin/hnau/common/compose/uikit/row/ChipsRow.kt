package hnau.common.compose.uikit.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.compose.uikit.shape.HnauShape
import hnau.common.compose.uikit.shape.create
import hnau.common.compose.uikit.shape.inRow
import hnau.common.compose.uikit.utils.Dimens

@Composable
fun <T> ChipsRow(
    all: List<T>,
    modifier: Modifier = Modifier,
    content: @Composable (item: T, shape: Shape) -> Unit,
) = Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(Dimens.chipsSeparation),
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
