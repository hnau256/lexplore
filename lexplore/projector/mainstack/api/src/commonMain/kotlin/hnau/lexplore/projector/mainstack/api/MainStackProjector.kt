package hnau.lexplore.projector.mainstack.api

import androidx.compose.runtime.Composable
import hnau.lexplore.model.mainstack.api.MainStackModel
import hnau.lexplore.projector.dictionaries.api.DictionariesProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

interface MainStackProjector {

    @Shuffle
    interface Dependencies {

        fun dictionaries(): DictionariesProjector.Dependencies
    }

    @Composable
    fun Content()

    fun interface Factory {

        fun createMainStackProjector(
            scope: CoroutineScope,
            dependencies: Dependencies,
            model: MainStackModel,
        ): MainStackProjector

        companion object
    }
}