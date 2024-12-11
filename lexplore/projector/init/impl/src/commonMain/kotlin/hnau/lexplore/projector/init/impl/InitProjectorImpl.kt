package hnau.lexplore.projector.init.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.goback.GoBackHandler
import hnau.common.compose.uikit.bubble.Content
import hnau.common.compose.uikit.bubble.SharedBubblesHolder
import hnau.common.kotlin.remindType
import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.projector.common.backbutton.BackButtonDelegate
import hnau.lexplore.projector.init.api.InitProjector
import hnau.lexplore.projector.mainstack.api.MainStackProjector
import kotlinx.coroutines.CoroutineScope

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

    @Composable
    override fun Content() = Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart,
    ) {
        //TODO content
        backButtonDelegate.Content()
        bubblesHolder.Content()
    }
}
