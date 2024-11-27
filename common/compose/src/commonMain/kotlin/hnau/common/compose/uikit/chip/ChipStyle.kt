package hnau.common.compose.uikit.chip

import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.ui.graphics.Color
import hnau.common.compose.uikit.utils.almostTransparent

data class ChipStyleColors(
    val background: Color,
    val border: Color,
    val contentWithTrailing: Color,
    val leading: Color,
)

interface ChipStyle {

    fun resolveColors(
        activeColor: Color,
        colors: Colors,
    ): ChipStyleColors

    companion object {

        val builder: BackgroundBuilder get() = BackgroundBuilderImpl

        val chip = builder
            .almostTransparentBackground
            .colorDefault
            .transparentBorder
            .almostActiveForeground

        val chipHighlighted = builder
            .almostTransparentBackground
            .colorActive
            .transparentBorder
            .activeForeground

        val chipSelected = builder
            .almostTransparentBackground
            .colorActive
            .activeBorder
            .activeForeground

        val button = builder
            .opaqueBackground
            .colorActive

        val default get() = chip

        fun chipSelected(
            selected: Boolean,
        ): ChipStyle = when (selected) {
            true -> chipSelected
            false -> chip
        }

        fun chipHighlighted(
            highlighted: Boolean,
        ): ChipStyle = when (highlighted) {
            true -> chipHighlighted
            false -> chip
        }
    }

    interface BackgroundBuilder {

        val transparentBackground: BorderBuilder

        val almostTransparentBackground: AlmostTransparentBackgroundBuilder

        val opaqueBackground: OpaqueBackgroundBuilder
    }

    interface AlmostTransparentBackgroundBuilder {

        val colorDefault: BorderBuilder

        val colorActive: BorderBuilder
    }

    interface OpaqueBackgroundBuilder {

        val colorDefault: ChipStyle

        val colorActive: ChipStyle
    }

    interface BorderBuilder {

        val transparentBorder: ForegroundBuilder

        val defaultBorder: ForegroundBuilder

        val activeBorder: ForegroundBuilder
    }

    interface ForegroundBuilder {

        val almostActiveForeground: ChipStyle

        val activeForeground: ChipStyle
    }
}

private data class Palette(
    val active: Color,
    val foreground: Color,
    val background: Color,
) {

    constructor(
        active: Color,
        colors: Colors,
    ) : this(
        active = active,
        foreground = colors.onSurface,
        background = colors.background,
    )

    val transparent = foreground.copy(alpha = 0f)

    fun getActiveOrForeground(
        active: Boolean,
    ): Color = when (active) {
        true -> this.active
        false -> this.foreground
    }
}

private inline fun resolveColors(
    activeColor: Color,
    colors: Colors,
    fromPalette: (Palette) -> ChipStyleColors,
): ChipStyleColors {
    return fromPalette(
        Palette(
            active = activeColor,
            colors = colors,
        ),
    )
}

private object BackgroundBuilderImpl : ChipStyle.BackgroundBuilder {

    override val transparentBackground: ChipStyle.BorderBuilder =
        BorderBuilderImpl(notOpaqueBackground = NotOpaqueBackgroundStyle.Transparent)

    override val almostTransparentBackground: ChipStyle.AlmostTransparentBackgroundBuilder
        get() = AlmostTransparentBackgroundBuilderImpl

    override val opaqueBackground: ChipStyle.OpaqueBackgroundBuilder
        get() = OpaqueBackgroundBuilderImpl
}

private object AlmostTransparentBackgroundBuilderImpl :
    ChipStyle.AlmostTransparentBackgroundBuilder {

    override val colorDefault: ChipStyle.BorderBuilder = BorderBuilderImpl(
        notOpaqueBackground = NotOpaqueBackgroundStyle.AlmostTransparent(
            color = BackgroundColor.Default,
        ),
    )

    override val colorActive: ChipStyle.BorderBuilder = BorderBuilderImpl(
        notOpaqueBackground = NotOpaqueBackgroundStyle.AlmostTransparent(
            color = BackgroundColor.Active,
        ),
    )
}

private object OpaqueBackgroundBuilderImpl : ChipStyle.OpaqueBackgroundBuilder {

    override val colorDefault: ChipStyle =
        OpaqueBackgroundChipStyleImpl(backgroundColor = BackgroundColor.Default)

    override val colorActive: ChipStyle =
        OpaqueBackgroundChipStyleImpl(backgroundColor = BackgroundColor.Active)
}

private class BorderBuilderImpl(
    notOpaqueBackground: NotOpaqueBackgroundStyle,
) : ChipStyle.BorderBuilder {

    override val transparentBorder: ChipStyle.ForegroundBuilder =
        NotOpaqueBackgroundForegroundBuilderImpl(
            notOpaqueBackground = notOpaqueBackground,
            border = BorderStyle.Transparent,
        )

    override val defaultBorder: ChipStyle.ForegroundBuilder =
        NotOpaqueBackgroundForegroundBuilderImpl(
            notOpaqueBackground = notOpaqueBackground,
            border = BorderStyle.Default,
        )

    override val activeBorder: ChipStyle.ForegroundBuilder =
        NotOpaqueBackgroundForegroundBuilderImpl(
            notOpaqueBackground = notOpaqueBackground,
            border = BorderStyle.Active,
        )
}

private class NotOpaqueBackgroundChipStyleImpl(
    private val notOpaqueBackground: NotOpaqueBackgroundStyle,
    private val border: BorderStyle,
    private val content: ContentStyle,
) : ChipStyle {

    override fun resolveColors(
        activeColor: Color,
        colors: Colors,
    ): ChipStyleColors = resolveColors(
        activeColor = activeColor,
        colors = colors,
    ) { palette ->
        ChipStyleColors(
            background = notOpaqueBackground.getBackgroundColor(palette),
            border = border.getColor(palette),
            contentWithTrailing = palette.getActiveOrForeground(
                active = when (content) {
                    ContentStyle.AlmostActive -> false
                    ContentStyle.Active -> true
                },
            ),
            leading = palette.getActiveOrForeground(active = true),
        )
    }
}

private class OpaqueBackgroundChipStyleImpl(
    private val backgroundColor: BackgroundColor,
) : ChipStyle {

    override fun resolveColors(
        activeColor: Color,
        colors: Colors,
    ): ChipStyleColors = resolveColors(
        activeColor = activeColor,
        colors = colors,
    ) { palette ->
        ChipStyleColors(
            background = backgroundColor.getOpaqueColor(palette),
            border = palette.transparent,
            contentWithTrailing = palette.background,
            leading = palette.background,
        )
    }
}

private class NotOpaqueBackgroundForegroundBuilderImpl(
    notOpaqueBackground: NotOpaqueBackgroundStyle,
    border: BorderStyle,
) : ChipStyle.ForegroundBuilder {

    override val almostActiveForeground: ChipStyle = NotOpaqueBackgroundChipStyleImpl(
        notOpaqueBackground = notOpaqueBackground,
        border = border,
        content = ContentStyle.AlmostActive,
    )

    override val activeForeground: ChipStyle = NotOpaqueBackgroundChipStyleImpl(
        notOpaqueBackground = notOpaqueBackground,
        border = border,
        content = ContentStyle.Active,
    )
}

private enum class ContentStyle {
    AlmostActive, Active
}

private enum class BorderStyle {
    Transparent, Default, Active,
    ;

    fun getColor(palette: Palette): Color = when (this) {
        Transparent -> palette.transparent
        Default -> palette.foreground
        Active -> palette.active
    }
}

private enum class BackgroundColor {
    Default, Active,
    ;

    fun getOpaqueColor(
        palette: Palette,
    ): Color = palette.getActiveOrForeground(
        active = when (this) {
            Default -> false
            Active -> true
        },
    )
}

private sealed interface NotOpaqueBackgroundStyle {

    fun getBackgroundColor(palette: Palette): Color

    object Transparent : NotOpaqueBackgroundStyle {

        override fun getBackgroundColor(palette: Palette) = palette.transparent
    }

    data class AlmostTransparent(val color: BackgroundColor) : NotOpaqueBackgroundStyle {

        override fun getBackgroundColor(palette: Palette) = color
            .getOpaqueColor(palette = palette)
            .copy(alpha = ContentAlpha.almostTransparent)
    }
}
