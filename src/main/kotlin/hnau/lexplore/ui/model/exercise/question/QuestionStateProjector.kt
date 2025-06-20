package hnau.lexplore.ui.model.exercise.question

import androidx.compose.runtime.Composable
import hnau.lexplore.ui.model.exercise.question.error.ErrorProjector
import hnau.lexplore.ui.model.exercise.question.input.InputProjector

sealed interface QuestionStateProjector {

    @Composable
    fun Content()

    data class Input(
        private val input: InputProjector,
    ) : QuestionStateProjector {

        @Composable
        override fun Content() {
            input.Content()
        }
    }

    data class Error(
        private val error: ErrorProjector,
    ) : QuestionStateProjector {

        @Composable
        override fun Content() {
            error.Content()
        }
    }
}