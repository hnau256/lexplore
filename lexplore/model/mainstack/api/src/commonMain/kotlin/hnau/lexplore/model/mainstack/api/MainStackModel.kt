package hnau.lexplore.model.mainstack.api

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

interface MainStackModel : GoBackHandlerProvider {

    @Serializable
    class Skeleton

    @Shuffle
    interface Dependencies

    fun interface Factory {

        fun createMainStackModel(
            scope: CoroutineScope,
            skeleton: Skeleton,
            dependencies: Dependencies,
        ): MainStackModel

        companion object
    }
}
