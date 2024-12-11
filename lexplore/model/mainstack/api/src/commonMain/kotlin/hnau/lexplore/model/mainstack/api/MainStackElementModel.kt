package hnau.lexplore.model.mainstack.api

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.lexplore.model.dictionaries.api.DictionariesModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface MainStackElementModel : GoBackHandlerProvider {

    data class Dictionaries(
        val dictionaries: DictionariesModel,
    ) : MainStackElementModel, GoBackHandlerProvider by dictionaries

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("dictionaries")
        data class Dictionaries(
            val dictionaries: DictionariesModel.Skeleton = DictionariesModel.Skeleton(),
        ) : Skeleton
    }
}
