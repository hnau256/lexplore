package hnau.lexplore.common.ui.uikit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipSize
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.progressindicator.chipInProgressLeadingContent
import kotlinx.coroutines.flow.StateFlow

@Composable
fun Button(
    onClick: (() -> Unit)?,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) = Chip(
    onClick = onClick,
    leading = leading,
    content = content,
    style = when (onClick) {
        null -> ChipStyle.chip
        else -> ChipStyle.button
    },
    size = ChipSize.large,
)

@Composable
fun Button(
    onClickOrExecuting: StateFlow<(() -> Unit)?>,
    content: @Composable () -> Unit,
) {
    val currentOnClickOrExecuting by onClickOrExecuting.collectAsState()
    Button(
        onClick = currentOnClickOrExecuting,
        leading = chipInProgressLeadingContent(currentOnClickOrExecuting == null),
        content = content,
    )
}
