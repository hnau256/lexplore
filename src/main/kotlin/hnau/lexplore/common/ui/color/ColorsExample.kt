package hnau.lexplore.common.ui.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hnau.lexplore.common.ui.color.material.MaterialHue
import hnau.lexplore.common.ui.uikit.row.ChipsFlowRow
import hnau.lexplore.common.ui.uikit.shape.HnauShape
import hnau.lexplore.common.ui.uikit.utils.Dimens

@Composable
private fun Foreground(
    foreground: Color,
    onForeground: Color,
    name: String,
) {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(
            containerColor = foreground,
            contentColor = onForeground,
        )
    ) {
        Text(name)
    }
}

@Composable
private fun Container(
    background: Color,
    onBackground: Color,
    name: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = background,
            contentColor = onBackground,
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.smallSeparation),
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            content()
        }
    }
}

@Composable
private fun ColorsExampleVariant(
    name: String,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.smallSeparation),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation)
        ) {
            Text(
                text = name,
            )
            Container(
                name = "PrimaryContainer",
                background = MaterialTheme.colorScheme.primaryContainer,
                onBackground = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Foreground(
                    foreground = MaterialTheme.colorScheme.primary,
                    onForeground = MaterialTheme.colorScheme.onPrimary,
                    name = "Primary"
                )
            }
            Container(
                name = "SecondaryContainer",
                background = MaterialTheme.colorScheme.secondaryContainer,
                onBackground = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Foreground(
                    foreground = MaterialTheme.colorScheme.secondary,
                    onForeground = MaterialTheme.colorScheme.onSecondary,
                    name = "Secondary"
                )
            }
            Container(
                name = "TertiaryContainer",
                background = MaterialTheme.colorScheme.tertiaryContainer,
                onBackground = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Foreground(
                    foreground = MaterialTheme.colorScheme.tertiary,
                    onForeground = MaterialTheme.colorScheme.onTertiary,
                    name = "Tertiary"
                )
            }
            Container(
                name = "ErrorContainer",
                background = MaterialTheme.colorScheme.errorContainer,
                onBackground = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Foreground(
                    foreground = MaterialTheme.colorScheme.error,
                    onForeground = MaterialTheme.colorScheme.onError,
                    name = "Error"
                )
            }
            Container(
                name = "Surface",
                background = MaterialTheme.colorScheme.surface,
                onBackground = MaterialTheme.colorScheme.onSurface,
            ) {
                Text(
                    text = "SurfaceTint",
                    color = MaterialTheme.colorScheme.surfaceTint,
                )
            }
            Container(
                name = "SurfaceVariant",
                background = MaterialTheme.colorScheme.surfaceVariant,
                onBackground = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(
                    text = "Scrim",
                    color = MaterialTheme.colorScheme.scrim,
                )
            }
            Container(
                name = "InverseSurface",
                background = MaterialTheme.colorScheme.inverseSurface,
                onBackground = MaterialTheme.colorScheme.inverseOnSurface,
            ) {
                Text(
                    text = "InversePrimary",
                    color = MaterialTheme.colorScheme.inversePrimary,
                )
            }
            OutlinedButton(
                onClick = {},
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.outlineVariant,
                )
            ) {
                Text("Outline/Variant")
            }
            ChipsFlowRow(
                all = listOf(
                    "tint" to MaterialTheme.colorScheme.surfaceTint,
                    "dim" to MaterialTheme.colorScheme.surfaceDim,
                    "bright" to MaterialTheme.colorScheme.surfaceBright,
                    "lowest" to MaterialTheme.colorScheme.surfaceContainerLowest,
                    "low" to MaterialTheme.colorScheme.surfaceContainerLow,
                    "container" to MaterialTheme.colorScheme.surfaceContainer,
                    "high" to MaterialTheme.colorScheme.surfaceContainerHigh,
                    "highest" to MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
            ) { (name, color) ->
                key(name) {

                    Text(
                        text = name,
                        modifier = Modifier
                            .background(
                                color = color,
                                shape = HnauShape(),
                            )
                            .padding(8.dp),
                        color = contentColorFor(color),
                    )
                }
            }
        }
    }
}


@Composable
private fun ColorsExampleVariant(
    isDark: Boolean,
    isDynamic: Boolean,
    modifier: Modifier = Modifier,
) {
    MaterialTheme(
        colorScheme = buildColors(
            primaryHue = MaterialHue.Yellow,
            secondaryHue = MaterialHue.DeepPurple,
            tertiaryHue = MaterialHue.Pink,
            isDark = isDark,
            isDynamic = isDynamic,
        )
    ) {
        val lightness = when (isDark) {
            true -> "Dark"
            false -> "Light"
        }
        val dynamicness = when (isDynamic) {
            true -> "Dynamic"
            false -> "Static"
        }
        ColorsExampleVariant(
            modifier = modifier,
            name = "$lightness$dynamicness"
        )
    }
}

@Preview(
    widthDp = 640,
    heightDp = 1280,
)
@Composable
fun ColorsExampleVariantPreview() {
    Row {
        ColorsExampleVariant(
            isDark = false,
            isDynamic = true,
            modifier = Modifier.weight(1f),
        )
        ColorsExampleVariant(
            isDark = true,
            isDynamic = true,
            modifier = Modifier.weight(1f),
        )
        ColorsExampleVariant(
            isDark = false,
            isDynamic = false,
            modifier = Modifier.weight(1f),
        )
        ColorsExampleVariant(
            isDark = true,
            isDynamic = false,
            modifier = Modifier.weight(1f),
        )
    }
}