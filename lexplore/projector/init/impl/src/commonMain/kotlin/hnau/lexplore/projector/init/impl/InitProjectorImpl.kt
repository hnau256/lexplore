package hnau.lexplore.projector.init.impl

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.goback.GoBackHandler
import hnau.common.compose.uikit.bubble.Content
import hnau.common.compose.uikit.bubble.SharedBubblesHolder
import hnau.common.compose.uikit.progressindicator.ProgressIndicator
import hnau.common.compose.uikit.progressindicator.ProgressIndicatorSize
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.remindType
import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.model.mainstack.api.MainStackModel
import hnau.lexplore.projector.common.backbutton.BackButtonDelegate
import hnau.lexplore.projector.init.api.InitProjector
import hnau.lexplore.projector.mainstack.api.MainStackProjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

internal class InitProjectorImpl(
    scope: CoroutineScope,
    private val dependencies: InitProjector.Dependencies,
    model: InitModel,
    private val mainStackProjectorFactory: MainStackProjector.Factory,
) : InitProjector {

    private val backButtonDelegate = BackButtonDelegate(
        scope = scope,
        goBackHandler = model
            .goBackHandler
            .remindType<GoBackHandler>(),
    )

    private val bubblesHolder = SharedBubblesHolder(
        scope = scope,
    )

    private val mainStackProjector: StateFlow<MainStackProjector?> = model
        .mainStack
        .remindType<StateFlow<MainStackModel?>>()
        .scopedInState(scope)
        .mapState(scope) { (mainStackScope, mainStackModelOrNull) ->
            mainStackModelOrNull?.let { mainStackModel ->
                mainStackProjectorFactory.createMainStackProjector(
                    scope = mainStackScope,
                    dependencies = dependencies.mainStack(
                        //bubblesShower = bubblesHolder,
                        //backButtonWidthProvider = backButtonDelegate,
                    ),
                    model = mainStackModel,
                )
            }
        }

    @Composable
    override fun Content() = Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart,
    ) {
        val loggableProjector by mainStackProjector.collectAsState()
        AnimatedContent(
            targetState = loggableProjector,
            contentKey = { it != null },
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { mainStackProjectorLocal ->
            when (mainStackProjectorLocal) {
                null -> InitializingContent()
                else -> mainStackProjectorLocal.Content()
            }
        }
        backButtonDelegate.Content()
        bubblesHolder.Content()
    }

    @Composable
    private fun InitializingContent() = Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ProgressIndicator(
            size = ProgressIndicatorSize.large,
        )
    }
}
