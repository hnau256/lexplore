package hnau.lexplore.projector.init.api

import androidx.compose.runtime.Composable
import hnau.common.compose.uikit.bubble.BubblesShower
import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.projector.common.Localizer
import hnau.lexplore.projector.common.backbutton.BackButtonWidthProvider
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

interface InitProjector {

    @Shuffle
    interface Dependencies {

        val localizer: Localizer

        companion object
    }

    @Composable
    fun Content()

    fun interface Factory {

        fun createInitProjector(
            scope: CoroutineScope,
            dependencies: Dependencies,
            model: InitModel,
        ): InitProjector

        companion object
    }
}