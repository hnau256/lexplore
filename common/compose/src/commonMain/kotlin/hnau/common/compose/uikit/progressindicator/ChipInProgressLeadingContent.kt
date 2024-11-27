package hnau.common.compose.uikit.progressindicator

import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable

fun chipInProgressLeadingContent(
    inProgress: Boolean,
): (@Composable () -> Unit)? = when (inProgress) {
    false -> null
    true -> {
        { ChipInProgressLeadingContent() }
    }
}

@Composable
fun ChipInProgressLeadingContent() = ProgressIndicator(
    size = ProgressIndicatorSize.small,
    color = LocalContentColor.current,
)
