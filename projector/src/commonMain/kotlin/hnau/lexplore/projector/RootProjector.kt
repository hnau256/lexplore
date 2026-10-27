package hnau.lexplore.projector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.uikit.backbutton.BackButtonHost
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.map
import hnau.lexplore.model.RootModel

class RootProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val model: RootModel,
) {

    @Pipe
    interface Dependencies {

        fun mainStack(
            localization: Localization,
        ): MainStackProjector.Dependencies

        companion object
    }

    private val mainStack = model
        .mainStack
        .mapWithScope(scope) { stackScope, mainStackOrLoading ->
            mainStackOrLoading.map { mainStackModel ->
                MainStackProjector(
                    scope = stackScope,
                    model = mainStackModel,
                    dependencies = dependencies.mainStack(
                        localization = Localization.default,
                    ),
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
        ) {
            BackButtonHost(
                contentPadding = contentPadding,
                goBackHandler = model.goBackHandler,
            ) { localContentPadding ->
                mainStack
                    .collectAsState()
                    .value
                    .LoadableContent(
                        transitionSpec = TransitionSpec.rememberCrossfade(),
                    ) { mainStackProjector ->
                        mainStackProjector.Content(
                            contentPadding = localContentPadding,
                        )
                    }
            }
        }
    }
}
