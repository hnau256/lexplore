package hnau.lexplore.ui.model

import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.ui.model.dictionaries.DictionariesModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface MainStackElementModel : GoBackHandlerProvider {

    data class Dictionaries(
        val dictionaries: DictionariesModel,
    ) : MainStackElementModel, GoBackHandlerProvider by dictionaries

    data class Exercise(
        val exercise: ExerciseModel,
    ) : MainStackElementModel, GoBackHandlerProvider by exercise

    data class Edit(
        val edit: EditModel,
    ) : MainStackElementModel, GoBackHandlerProvider by edit

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("dictionaries")
        data class Dictionaries(
            val dictionaries: DictionariesModel.Skeleton = DictionariesModel.Skeleton(),
        ) : Skeleton

        @Serializable
        @SerialName("exercise")
        data class Exercise(
            val exercise: ExerciseModel.Skeleton,
        ) : Skeleton {

            constructor(
                words: List<Word>,
            ) : this(
                exercise = ExerciseModel.Skeleton(
                    words = words,
                )
            )
        }

        @Serializable
        @SerialName("edit")
        data class Edit(
            val edit: EditModel.Skeleton,
        ) : Skeleton {

            constructor(
                dictionaryName: String,
                words: List<Word>,
            ) : this(
                edit = EditModel.Skeleton(
                    dictionaryName = dictionaryName,
                    words = words,
                )
            )
        }
    }
}
