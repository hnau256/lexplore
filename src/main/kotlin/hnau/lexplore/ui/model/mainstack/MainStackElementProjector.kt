package hnau.lexplore.ui.model.mainstack

import androidx.compose.runtime.Composable
import hnau.lexplore.ui.model.dictionaries.DictionariesProjector
import hnau.lexplore.ui.model.edit.EditProjector
import hnau.lexplore.ui.model.exercise.ExerciseProjector

sealed interface MainStackElementProjector {

    @Composable
    fun Content()

    data class Dictionaries(
        private val dictionaries: DictionariesProjector
    ): MainStackElementProjector {

        @Composable
        override fun Content() {
            dictionaries.Content()
        }
    }

    data class Edit(
        private val edit: EditProjector
    ): MainStackElementProjector {

        @Composable
        override fun Content() {
            edit.Content()
        }
    }

    data class Exercise(
        private val exercise: ExerciseProjector
    ): MainStackElementProjector {

        @Composable
        override fun Content() {
            exercise.Content()
        }
    }
}