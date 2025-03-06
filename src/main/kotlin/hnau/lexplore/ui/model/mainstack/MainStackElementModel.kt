package hnau.lexplore.ui.model.mainstack

import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import hnau.lexplore.ui.model.dictionaries.DictionariesModel
import hnau.lexplore.ui.model.edit.EditModel
import hnau.lexplore.ui.model.exercise.ExerciseModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface MainStackElementModel : GoBackHandlerProvider {

    val key: Any?

    data class Dictionaries(
        val dictionaries: DictionariesModel,
    ) : MainStackElementModel, GoBackHandlerProvider by dictionaries {

        override val key: Any
            get() = 0
    }

    data class Exercise(
        val exercise: ExerciseModel,
    ) : MainStackElementModel, GoBackHandlerProvider by exercise {

        override val key: Any
            get() = 1
    }

    data class Edit(
        val edit: EditModel,
    ) : MainStackElementModel, GoBackHandlerProvider by edit {

        override val key: Any
            get() = 2
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("data/dictionaries")
        data class Dictionaries(
            val dictionaries: DictionariesModel.Skeleton = DictionariesModel.Skeleton(),
        ) : Skeleton

        @Serializable
        @SerialName("exercise")
        data class Exercise(
            val exercise: ExerciseModel.Skeleton,
        ) : Skeleton {

            constructor(
                words: Set<DictionaryName>,
            ) : this(
                exercise = ExerciseModel.Skeleton(
                    dictionaries = words,
                )
            )
        }

        @Serializable
        @SerialName("edit")
        data class Edit(
            val edit: EditModel.Skeleton,
        ) : Skeleton {

            constructor(
                name: DictionaryName,
            ) : this(
                edit = EditModel.Skeleton(
                    name = name,
                )
            )
        }
    }
}
