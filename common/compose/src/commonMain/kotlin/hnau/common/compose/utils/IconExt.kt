package hnau.common.compose.utils

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.Icon as PlatformIcon

@Composable
inline fun Icon(
    modifier: Modifier = Modifier,
    chooseIcon: Icons.Filled.() -> ImageVector,
) = PlatformIcon(
    modifier = modifier,
    imageVector = Icons.Filled.chooseIcon(),
    contentDescription = null,
)
