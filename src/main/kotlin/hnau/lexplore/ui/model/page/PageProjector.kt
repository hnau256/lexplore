package hnau.lexplore.ui.model.page

import androidx.compose.runtime.Composable
import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.ui.uikit.Content
import hnau.lexplore.ui.model.question.QuestionProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class PageProjector(
    scope: CoroutineScope,
    model: PageModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun question(): QuestionProjector.Dependencies
    }

    private val question: StateFlow<Loadable<QuestionProjector>> = model
        .question
        .mapWithScope(scope) { questionScope, questionOrLoading ->
            questionOrLoading.map { question ->
                QuestionProjector(
                    scope = questionScope,
                    model = question,
                    dependencies = dependencies.question(),
                )
            }
        }

    @Composable
    fun Content() {
        question.Content { question ->
            question.Content()
        }
    }
}