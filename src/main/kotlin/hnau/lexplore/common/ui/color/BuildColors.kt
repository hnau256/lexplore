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


@Composable
fun buildColors(
    primaryHue: MaterialHue,
    secondaryHue: MaterialHue,
    tertiaryHue: MaterialHue,
    isDark: Boolean = isSystemInDarkTheme(),
    isDynamic: Boolean = true,
): ColorScheme = when {
    isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> buildDynamicColors(
        isDark = isDark,
    )

    else -> buildStaticColors(
        primaryHue = primaryHue,
        secondaryHue = secondaryHue,
        tertiaryHue = tertiaryHue,
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

private val errorHue = MaterialHue.Red

@Composable
private fun buildStaticColors(
    primaryHue: MaterialHue,
    secondaryHue: MaterialHue,
    tertiaryHue: MaterialHue,
    isDark: Boolean,
): ColorScheme {

    val lightness = when (isDark) {
        true -> Lightness.Collection.dark
        false -> Lightness.Collection.light
    }

    fun chooseColor(
        hue: MaterialHue,
        direction: Lightness.Direction,
    ): Color {
        val materialLightness = lightness[direction]
        return MaterialColors[hue][materialLightness].compose
    }

    val primary = chooseColor(hue = primaryHue, direction = Lightness.Direction.Far)
    val onPrimary = chooseColor(hue = primaryHue, direction = Lightness.Direction.Near)
    val secondary = chooseColor(hue = secondaryHue, direction = Lightness.Direction.Far)
    val onSecondary = chooseColor(hue = secondaryHue, direction = Lightness.Direction.Near)
    val tertiary = chooseColor(hue = tertiaryHue, direction = Lightness.Direction.Far)
    val onTertiary = chooseColor(hue = tertiaryHue, direction = Lightness.Direction.Near)
    val primaryContainer = chooseColor(hue = primaryHue, direction = Lightness.Direction.Near)
    val onPrimaryContainer = chooseColor(hue = primaryHue, direction = Lightness.Direction.Far)
    val secondaryContainer = chooseColor(hue = secondaryHue, direction = Lightness.Direction.Near)
    val onSecondaryContainer =
        chooseColor(hue = secondaryHue, direction = Lightness.Direction.Far)
    val tertiaryContainer = chooseColor(hue = tertiaryHue, direction = Lightness.Direction.Near)
    val onTertiaryContainer =
        chooseColor(hue = tertiaryHue, direction = Lightness.Direction.Far)
    val surface = lightness[0.1]
    val onSurface = lightness[0.9]
    val surfaceVariant = lightness[0.2]
    val onSurfaceVariant = lightness[0.8]
    val background = lightness[0.02]
    val onBackground = lightness[0.98]
    val error = chooseColor(hue = errorHue, direction = Lightness.Direction.Far)
    val onError = chooseColor(hue = errorHue, direction = Lightness.Direction.Near)
    val inverseSurface = lightness[0.9]
    val inverseOnSurface = lightness[0.1]
    val inversePrimary = chooseColor(hue = primaryHue, direction = Lightness.Direction.Near)
    val outline = lightness[0.9]
    val outlineVariant = lightness[1]
    val surfaceTint = chooseColor(hue = primaryHue, direction = Lightness.Direction.Far)
    val scrim = lightness[1]
    val surfaceDim = lightness[0.05]
    val surfaceBright = lightness[0.1]
    val surfaceLowest = lightness[0]
    val surfaceLow = lightness[0.05]
    val surfaceContainer = lightness[0.09]
    val surfaceHigh = lightness[0.13]
    val surfaceHigest = lightness[0.17]

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
            surfaceContainerLowest = surfaceLowest,
            surfaceContainerLow = surfaceLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceHigh,
            surfaceContainerHighest = surfaceHigest,
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
            surfaceContainerLowest = surfaceLowest,
            surfaceContainerLow = surfaceLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceHigh,
            surfaceContainerHighest = surfaceHigest,
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
                almostFar = MaterialLightness.V100,
                far = MaterialLightness.V50,
                positiveLightness = true,
            )

            val light = Collection(
                near = MaterialLightness.V50,
                almostNear = MaterialLightness.V100,
                almostFar = MaterialLightness.V800,
                far = MaterialLightness.V900,
                positiveLightness = false,
            )
        }
    }
    
}
