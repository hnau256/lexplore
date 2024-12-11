package hnau.lexplore.model.dictionaries.api

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.lexplore.data.api.dictionary.dto.DictionariesFlow
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

interface DictionariesModel : GoBackHandlerProvider {

    @Serializable
    class Skeleton

    @Shuffle
    interface Dependencies {

        val dictionariesFlow: DictionariesFlow
    }

    fun interface Factory {

        fun createDictionariesModel(
            scope: CoroutineScope,
            skeleton: Skeleton,
            dependencies: Dependencies,
        ): DictionariesModel

        companion object
    }
}
