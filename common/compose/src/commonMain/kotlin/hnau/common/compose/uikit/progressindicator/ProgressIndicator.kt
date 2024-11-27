package hnau.common.compose.uikit.progressindicator

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import hnau.common.compose.uikit.progressindicator.utils.RoundCapCircularProgressIndicator

@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    size: ProgressIndicatorSize = ProgressIndicatorSize.default,
    color: Color = MaterialTheme.colors.primary,
) = RoundCapCircularProgressIndicator(
    modifier = modifier,
    diameter = size.diameter,
    color = color,
    strokeWidth = size.strokeWidth,
)
