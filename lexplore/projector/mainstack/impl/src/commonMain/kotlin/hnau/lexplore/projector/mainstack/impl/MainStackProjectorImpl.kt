package hnau.lexplore.projector.mainstack.impl

import androidx.compose.runtime.Composable
import hnau.lexplore.model.mainstack.api.MainStackModel
import hnau.lexplore.projector.mainstack.api.MainStackProjector
import kotlinx.coroutines.CoroutineScope

internal class MainStackProjectorImpl(
    scope: CoroutineScope,
    private val dependencies: MainStackProjector.Dependencies,
    model: MainStackModel,
) : MainStackProjector {

    @Composable
    override fun Content() {

    }
}
