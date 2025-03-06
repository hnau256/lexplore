package hnau.lexplore.ui.model.mainstack

import androidx.compose.runtime.Composable
import hnau.lexplore.ui.model.dictionaries.DictionariesProjector
import hnau.lexplore.ui.model.edit.EditProjector
import hnau.lexplore.ui.model.exercise.ExerciseProjector

sealed interface MainStackElementProjector {

    @Composable
    fun Content()

    val key: Any?

    data class Dictionaries(
        private val dictionaries: DictionariesProjector,
    ) : MainStackElementProjector {

        @Composable
        override fun Content() {
            dictionaries.Content()
        }

        override val key: Any
            get() = 0
    }

    data class Edit(
        private val edit: EditProjector,
    ) : MainStackElementProjector {

        @Composable
        override fun Content() {
            edit.Content()
        }

        override val key: Any
            get() = 1
    }

    data class Exercise(
        private val exercise: ExerciseProjector,
    ) : MainStackElementProjector {

        @Composable
        override fun Content() {
            exercise.Content()
        }

        override val key: Any
            get() = 2
    }
}