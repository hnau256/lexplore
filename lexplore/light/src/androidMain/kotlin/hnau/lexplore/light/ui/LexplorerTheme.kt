package hnau.lexplore.light.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import hnau.common.color.material.MaterialHue

@Composable
fun LexplorerTheme(
    content: @Composable () -> Unit,
) = MaterialTheme(
    colors = buildColors(
        primaryHue = MaterialHue.LightGreen,
        secondaryHue = MaterialHue.Orange,
        isDark = isSystemInDarkTheme(),
    ),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colors.onBackground,
            LocalDensity provides Density(LocalDensity.current.density * 1.25f),
        ) {
            content()
        }
    }
}
