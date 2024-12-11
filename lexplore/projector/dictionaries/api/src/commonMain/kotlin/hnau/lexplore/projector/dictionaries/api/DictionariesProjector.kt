package hnau.lexplore.projector.dictionaries.api

import androidx.compose.runtime.Composable
import hnau.lexplore.model.dictionaries.api.DictionariesModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

interface DictionariesProjector {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content()

    fun interface Factory {

        fun createDictionariesProjector(
            scope: CoroutineScope,
            dependencies: Dependencies,
            model: DictionariesModel,
        ): DictionariesProjector

        companion object
    }
}