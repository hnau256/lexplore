package hnau.lexplore.common.ui.uikit.chip

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import hnau.lexplore.common.ui.uikit.TripleRow
import hnau.lexplore.common.ui.uikit.chip.utils.ChipSide
import hnau.lexplore.common.ui.uikit.shape.HnauShape
import hnau.lexplore.common.ui.uikit.utils.Dimens

@Composable
fun Chip(
    modifier: Modifier = Modifier,
    size: ChipSize = ChipSize.default,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    style: ChipStyle = ChipStyle.default,
    shape: Shape = HnauShape(),
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val materialColors = MaterialTheme.colorScheme
    val colors = remember(style, activeColor, materialColors) {
        style.resolveColors(
            activeColor = activeColor,
            colors = materialColors,
        )
    }
    TripleRow(
        contentAlignment = Alignment.Start,
        modifier = modifier
            .height(size.height)
            .border(
                width = Dimens.border,
                shape = shape,
                color = animateColorAsState(colors.border).value,
            )
            .clip(shape)
            .then(
                when (onClick != null) {
                    true -> Modifier.clickable(onClick = onClick)
                    else -> Modifier
                },
            )
            .background(
                color = animateColorAsState(colors.background).value,
            ),
        leading = {
            ChipSide(
                side = ChipSide.Leading,
                size = size,
                contentColor = animateColorAsState(colors.leading).value,
                content = leading,
            )
        },
        content = {
            CompositionLocalProvider(
                LocalContentColor provides animateColorAsState(colors.contentWithTrailing).value,
                LocalTextStyle provides size.getTextStyle(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) { content() }
            }
        },
        trailing = {
            ChipSide(
                side = ChipSide.Trailing,
                size = size,
                contentColor = animateColorAsState(colors.contentWithTrailing).value,
                content = trailing,
            )
        },
    )
}
