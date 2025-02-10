package hnau.lexplore.common.ui.uikit.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import hnau.lexplore.common.ui.uikit.utils.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipsFlowRow(
    all: List<T>,
    modifier: Modifier = Modifier,
    mainAxisSpacing: Dp = Dimens.separation,
    crossAxisSpacing: Dp = Dimens.separation,
    content: @Composable (item: T) -> Unit,
) = FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
    verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
) {
    all.forEach { item -> content(item) }
}
