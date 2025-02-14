package hnau.lexplore.ui.model.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.kotlin.LoadableStateFlow
import hnau.lexplore.common.kotlin.coroutines.flatMapState
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.getOrInit
import hnau.lexplore.common.kotlin.toAccessor
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.model.goback.NeverGoBackHandler
import hnau.lexplore.common.ui.uikit.Content
import hnau.lexplore.exercise.Engine
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.ui.model.question.QuestionModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class PageModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val engine: Engine,
    private val switchToNextQuestion: (wordToExclude: WordToLearn) -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val wordToExclude: WordToLearn? = null,
        var question: QuestionModel.Skeleton? = null,
    )

    @Shuffle
    interface Dependencies {

        fun question(): QuestionModel.Dependencies
    }

    val question: StateFlow<Loadable<QuestionModel>> = LoadableStateFlow(scope) {
        engine.generateNewQuestion(
            wordToExclude = skeleton.wordToExclude,
        )
    }
        .mapWithScope(scope) { questionScope, questionOrLoading ->
            questionOrLoading.map { question ->
                QuestionModel(
                    scope = questionScope,
                    dependencies = dependencies.question(),
                    skeleton = skeleton::question
                        .toAccessor()
                        .getOrInit { QuestionModel.Skeleton() },
                    question = question,
                    switchToNextQuestion = { switchToNextQuestion(question.word.toLearn) }
                )
            }
        }


    override val goBackHandler: StateFlow<(() -> Unit)?> = question
        .flatMapState(scope) { currentQuestionModel ->
            currentQuestionModel.fold(
                ifLoading = { NeverGoBackHandler },
                ifReady = GoBackHandlerProvider::goBackHandler,
            )
        }
}