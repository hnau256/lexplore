package hnau.lexplore.common.ui.uikit.progressindicator

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import hnau.lexplore.common.ui.uikit.progressindicator.utils.RoundCapCircularProgressIndicator

@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    size: ProgressIndicatorSize = ProgressIndicatorSize.default,
    color: Color = MaterialTheme.colorScheme.primary,
) = RoundCapCircularProgressIndicator(
    modifier = modifier,
    diameter = size.diameter,
    color = color,
    strokeWidth = size.strokeWidth,
)
