@file:UseSerializers(
    MutableStateFlowSerializer::class,
    NonEmptySetSerializer::class,
)

package hnau.lexplore.model

import arrow.core.NonEmptySet
import arrow.core.serialization.NonEmptySetSerializer
import hnau.lexplore.common.DictionaryWord
import hnau.lexplore.common.dictionary.Dictionaries
import hnau.lexplore.common.dictionary.DictionaryName
import hnau.lexplore.common.exercise.Engine
import hnau.lexplore.common.exercise.ExerciseWords
import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.model.exercise.ExerciseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.LoadableStateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.fold
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.commons.kotlin.toAccessor

class ExercisePrepareModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val dictionaries: Dictionaries

        val knowledgeRepository: KnowledgeRepository

        fun exercise(
            engine: Engine,
            words: ExerciseWords,
        ): ExerciseModel.Dependencies

    }

    @Serializable
    data class Skeleton(
        val dictionaries: NonEmptySet<DictionaryName>,
        var exercise: ExerciseModel.Skeleton? = null,
    )

    val exercise: StateFlow<Loadable<ExerciseModel>> = LoadableStateFlow(
        scope = scope,
    ) {
        val words: ExerciseWords = skeleton
            .dictionaries
            .toSet()
            .flatMap { dictionaryName ->
                dependencies
                    .dictionaries[dictionaryName]
                    .dictionaryWords
            }
            .sortedByDescending(DictionaryWord::weight)
            .associate { dictionaryWord ->
                dictionaryWord.toLearn to dictionaryWord.translation
            }
            .let(::ExerciseWords)

        val engine = Engine(
            wordsToLearn = words.words.keys.toList(),
            knowledgeRepository = dependencies.knowledgeRepository,
        )

        ExerciseModel(
            scope = scope,
            skeleton = skeleton::exercise
                .toAccessor()
                .getOrInit { ExerciseModel.Skeleton() },
            dependencies = dependencies.exercise(
                engine = engine,
                words = words,
            ),
        )
    }

    val goBackHandler: GoBackHandler = derivedStateFlowOf(scope) {
        exercise.state.fold(
            ifLoading = { null },
            ifReady = { exercise ->
                exercise.goBackHandler.state
            },
        )
    }
}