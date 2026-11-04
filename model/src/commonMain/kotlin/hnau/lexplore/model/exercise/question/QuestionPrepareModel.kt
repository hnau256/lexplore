package hnau.lexplore.model.exercise.question

import hnau.lexplore.common.WordToLearn
import hnau.lexplore.common.exercise.Engine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.LoadableStateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.fold
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.toAccessor

class QuestionPrepareModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    onReady: (WordToLearn) -> Unit,
) {

    @Pipe
    interface Dependencies {

        val engine: Engine

        fun question(
            wordToLearn: WordToLearn,
        ): QuestionModel.Dependencies

    }

    @Serializable
    data class Skeleton(
        val previousWord: WordToLearn?,
        var question: QuestionModel.Skeleton? = null,
    ) {

        companion object {

            fun createFirst(): Skeleton = Skeleton(
                previousWord = null,
            )

            fun createNext(
                previousWord: WordToLearn,
            ): Skeleton = Skeleton(
                previousWord = previousWord,
            )
        }
    }

    val question: StateFlow<Loadable<QuestionModel>> = LoadableStateFlow(
        scope = scope,
    ) {

        val previousWord = skeleton
            .previousWord

        val wordToLearn = dependencies
            .engine
            .findNextWordToLearn(
                wordToLearnToExclude = previousWord,
            )

        QuestionModel(
            scope = scope,
            dependencies = dependencies.question(
                wordToLearn = wordToLearn,
            ),
            skeleton = skeleton::question
                .toAccessor()
                .getOrInit { QuestionModel.Skeleton() },
            previousWord = previousWord,
            onReady = { onReady(wordToLearn) },
        )
    }


    val goBackHandler: GoBackHandler = derivedStateFlowOf(scope) {
        question.state.fold(
            ifLoading = { null },
            ifReady = { question ->
                question.goBackHandler.state
            },
        )
    }
}