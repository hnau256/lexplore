package hnau.common.compose.uikit.chip.utils

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import hnau.common.compose.uikit.chip.ChipSize
import hnau.common.compose.utils.AnimatedNullableVisibility
import hnau.common.compose.utils.AnimatedVisibilityTransitions

internal enum class ChipSide { Leading, Trailing }

@Composable
internal fun ChipSide(
    side: ChipSide,
    size: ChipSize,
    contentColor: Color,
    content: (@Composable () -> Unit)?,
) = Row {
    ChipSideSeparator(
        targetWidth = when (content) {
            null -> size.contentEdgeSeparation / 2
            else -> when (side) {
                ChipSide.Leading -> size.sideContentEdgeSeparation
                ChipSide.Trailing -> size.contentSideContentSeparation
            }
        },
    )
    AnimatedNullableVisibility(
        value = content,
        transitions = AnimatedVisibilityTransitions.horizontal,
    ) { contentLocal ->
        Box(
            modifier = Modifier
                .size(size.sideContentSize),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                contentLocal()
            }
        }
    }
    ChipSideSeparator(
        targetWidth = when (content) {
            null -> size.contentEdgeSeparation / 2
            else -> when (side) {
                ChipSide.Leading -> size.contentSideContentSeparation
                ChipSide.Trailing -> size.sideContentEdgeSeparation
            }
        },
    )
}

@Composable
private fun ChipSideSeparator(
    targetWidth: Dp,
) {
    val width by animateDpAsState(targetWidth)
    Spacer(modifier = Modifier.width(width))
}
