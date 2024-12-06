package hnau.lexplore.model.init.api

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

interface InitModel : GoBackHandlerProvider {

    @Serializable
    class Skeleton

    @Shuffle
    interface Dependencies

    fun interface Factory {

        fun createInitModel(
            scope: CoroutineScope,
            skeleton: Skeleton,
            dependencies: Dependencies,
        ): InitModel

        companion object
    }
}
