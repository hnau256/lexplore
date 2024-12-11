package hnau.lexplore.model.init.api

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.lexplore.data.api.dictionary.DictionaryRepository
import hnau.lexplore.data.api.dictionary.dto.DictionariesFlow
import hnau.lexplore.model.mainstack.api.MainStackModel
import hnau.lexplore.prefiller.api.PrefillDataProvider
import hnau.lexplore.prefiller.api.Prefiller
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface InitModel : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        var mainStack: MainStackModel.Skeleton? = null,
    )

    @Shuffle
    interface Dependencies {

        val dictionariesRepository: DictionaryRepository

        val prefillDataProvider: PrefillDataProvider

        val prefiller: Prefiller

        fun mainStack(
            dictionariesFlow: DictionariesFlow,
        ): MainStackModel.Dependencies
    }

    val mainStack: StateFlow<MainStackModel?>

    fun interface Factory {

        fun createInitModel(
            scope: CoroutineScope,
            skeleton: Skeleton,
            dependencies: Dependencies,
        ): InitModel

        companion object
    }
}
