package hnau.lexplore.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.remindSupertype
import hnau.common.kotlin.remindType
import hnau.lexplore.app.LexploreApp
import hnau.lexplore.compose.utils.KtIotTheme
import hnau.lexplore.compose.utils.LocalizerImpl
import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.projector.common.Localizer
import hnau.lexplore.projector.init.api.InitProjector
import hnau.lexplore.projector.init.api.impl

@Composable
fun LexploreApp.Content(
    localizer: Localizer = LocalizerImpl,
) {
    val projectorScope = rememberCoroutineScope()
    val projector = remember {
        InitProjector
            .Factory
            .assemble()
            .createInitProjector(
                scope = projectorScope,
                dependencies = InitProjector.Dependencies.impl(
                    localizer = localizer,
                ),
                model = model
                    .remindType<InitModel>()
                    .remindSupertype<_, GoBackHandlerProvider>(),
            )
    }
    KtIotTheme {
        projector.Content()
    }
}
