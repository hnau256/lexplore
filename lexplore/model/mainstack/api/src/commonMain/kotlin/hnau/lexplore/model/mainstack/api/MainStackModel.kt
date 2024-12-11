package hnau.lexplore.model.mainstack.api

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.model.dictionaries.api.DictionariesModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface MainStackModel : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val stack: MutableStateFlow<NonEmptyStack<MainStackElementModel.Skeleton>> =
            MutableStateFlow(NonEmptyStack(MainStackElementModel.Skeleton.Dictionaries())),
    )

    @Shuffle
    interface Dependencies {

        fun dictionaries(): DictionariesModel.Dependencies
    }

    val stack: StateFlow<NonEmptyStack<MainStackElementModel>>

    fun interface Factory {

        fun createMainStackModel(
            scope: CoroutineScope,
            skeleton: Skeleton,
            dependencies: Dependencies,
        ): MainStackModel

        companion object
    }
}
