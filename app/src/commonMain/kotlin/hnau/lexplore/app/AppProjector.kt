package hnau.lexplore.app

import androidx.compose.runtime.CompositionLocalProvider
import hnau.lexplore.model.RootModel
import hnau.lexplore.projector.RootProjector
import hnau.lexplore.projector.impl
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.model.app.AppModel
import org.hnau.commons.app.model.theme.ThemeBrightness
import org.hnau.commons.app.model.theme.color.Hue
import org.hnau.commons.app.model.theme.palette.SystemPalettes
import org.hnau.commons.app.projector.app.AppProjector
import org.hnau.commons.app.projector.fractal.context.FContext
import org.hnau.commons.app.projector.fractal.context.LocalFContext

fun createAppProjector(
    scope: CoroutineScope,
    model: AppModel<RootModel, RootModel.Skeleton>,
    createSystemPalettes: (ThemeBrightness) -> SystemPalettes,
): AppProjector<RootModel, RootModel.Skeleton, RootProjector> = AppProjector(
    scope = scope,
    model = model,
    createProjector = { projectorScope, rootModel ->
        RootProjector(
            scope = projectorScope,
            model = rootModel,
            dependencies = RootProjector.Dependencies.impl(),
        )
    },
    createSystemPalettes = createSystemPalettes,
    fallbackHue = Hue(300),
    content = { rootProjector, contentPadding, palettes ->
        CompositionLocalProvider(
            LocalFContext provides FContext.createBase(
                palettes = palettes,
            )
        ) {
            rootProjector.Content(
                contentPadding = contentPadding,
            )
        }
    },
)
