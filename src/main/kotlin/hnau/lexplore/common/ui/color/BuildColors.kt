package hnau.lexplore.common.ui.color

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import hnau.lexplore.common.ui.color.material.MaterialColors
import hnau.lexplore.common.ui.color.material.MaterialHue
import hnau.lexplore.common.ui.color.material.MaterialLightness
import hnau.lexplore.common.ui.color.material.get
import scheme.Scheme


@Composable
fun buildColors(
    primaryHue: MaterialHue,
    isDark: Boolean = isSystemInDarkTheme(),
    tryUseDynamicColors: Boolean = true,
): ColorScheme = when {
    tryUseDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> buildDynamicColors(
        isDark = isDark,
    )

    else -> buildStaticColors(
        primaryHue = primaryHue,
        isDark = isDark,
    )
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun buildDynamicColors(
    isDark: Boolean,
): ColorScheme {
    val context = LocalContext.current
    return when (isDark) {
        true -> dynamicDarkColorScheme(context)
        false -> dynamicLightColorScheme(context)
    }
}

@Composable
private fun buildStaticColors(
    primaryHue: MaterialHue,
    isDark: Boolean,
): ColorScheme {
    val argb = MaterialColors[primaryHue].v600.let { color ->
        color.r.toInt().shl(16) +
                color.g.toInt().shl(8) +
                color.b.toInt().shl(0)
    }
    val scheme = when (isDark) {
        true -> Scheme.dark(argb)
        false -> Scheme.light(argb)
    }

    val primary = Color(scheme.primary)
    val onPrimary = Color(scheme.onPrimary)
    val secondary = Color(scheme.secondary)
    val onSecondary = Color(scheme.onSecondary)
    val tertiary = Color(scheme.tertiary)
    val onTertiary = Color(scheme.onTertiary)
    val primaryContainer = Color(scheme.primaryContainer)
    val onPrimaryContainer = Color(scheme.onPrimaryContainer)
    val secondaryContainer = Color(scheme.secondaryContainer)
    val onSecondaryContainer = Color(scheme.onSecondaryContainer)
    val tertiaryContainer = Color(scheme.tertiaryContainer)
    val onTertiaryContainer = Color(scheme.onTertiaryContainer)
    val surface = Color(scheme.surface)
    val onSurface = Color(scheme.onSurface)
    val surfaceVariant = Color(scheme.surfaceVariant)
    val onSurfaceVariant = Color(scheme.onSurfaceVariant)
    val surfaceContainerLowest = Color(scheme.surface)
    val surfaceContainerLow = Color(scheme.surface)
    val surfaceContainer = Color(scheme.surface)
    val surfaceContainerHigh = Color(scheme.surface)
    val surfaceContainerHighest = Color(scheme.surface)
    val surfaceBright = Color(scheme.surface)
    val surfaceDim = Color(scheme.surface)
    val surfaceTint = Color(scheme.surface)
    val inverseSurface = Color(scheme.inverseSurface)
    val inverseOnSurface = Color(scheme.inverseOnSurface)
    val inversePrimary = Color(scheme.inversePrimary)
    val background = Color(scheme.background)
    val onBackground = Color(scheme.onBackground)
    val error = Color(scheme.error)
    val onError = Color(scheme.onError)
    val outline = Color(scheme.outline)
    val outlineVariant = Color(scheme.outlineVariant)
    val scrim = Color(scheme.scrim)

    return when (isDark) {
        true -> darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            onTertiary = onTertiary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceBright = surfaceBright,
            surfaceDim = surfaceDim,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            background = background,
            onBackground = onBackground,
            error = error,
            onError = onError,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
        )

        false -> lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            onTertiary = onTertiary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceBright = surfaceBright,
            surfaceDim = surfaceDim,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            background = background,
            onBackground = onBackground,
            error = error,
            onError = onError,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
        )
    }
}

private object Lightness {

    enum class Direction { Near, AlmostNear, AlmostFar, Far }

    data class Collection(
        val near: MaterialLightness,
        val almostNear: MaterialLightness,
        val almostFar: MaterialLightness,
        val far: MaterialLightness,
        val positiveLightness: Boolean,
    ) {

        operator fun get(
            direction: Direction,
        ): MaterialLightness = when (direction) {
            Direction.Near -> near
            Direction.AlmostNear -> almostNear
            Direction.AlmostFar -> almostFar
            Direction.Far -> far
        }

        operator fun get(
            lightnessPercentage: Number,
        ): Color {
            val (min, max) = when (positiveLightness) {
                true -> 0f to 1f
                false -> 1f to 0f
            }
            val lightness = min + (max - min) * lightnessPercentage.toFloat()
            return Color(
                red = lightness,
                green = lightness,
                blue = lightness,
                alpha = 1f,
            )
        }


        companion object {

            val dark = Collection(
                near = MaterialLightness.V900,
                almostNear = MaterialLightness.V800,
                almostFar = MaterialLightness.V200,
                far = MaterialLightness.V100,
                positiveLightness = true,
            )

            val light = Collection(
                near = MaterialLightness.V50,
                almostNear = MaterialLightness.V100,
                almostFar = MaterialLightness.V700,
                far = MaterialLightness.V900,
                positiveLightness = false,
            )
        }
    }

}
