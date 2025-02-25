package hnau.lexplore.ui.model.exercise

import hnau.lexplore.common.kotlin.coroutines.flatMapState
import hnau.lexplore.common.kotlin.coroutines.mapState
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.coroutines.scopedInState
import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.Engine
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import hnau.lexplore.ui.model.page.PageModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

class ExerciseModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val goBack: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val page: MutableStateFlow<PageModel.Skeleton> =
            MutableStateFlow(PageModel.Skeleton()),
        @Serializable(MutableStateFlowSerializer::class)
        val displayConfirmGoBack: MutableStateFlow<Boolean> =
            MutableStateFlow(false),
        val dictionaries: Set<DictionaryName>,
    )

    @Shuffle
    interface Dependencies {

        val knowledgeRepository: KnowledgeRepository

        val dictionaries: Dictionaries

        fun question(): PageModel.Dependencies
    }

    val displayConfirmGoBack: StateFlow<Boolean>
        get() = skeleton.displayConfirmGoBack

    private val engine = Engine(
        words = skeleton
            .dictionaries
            .flatMap { name ->
                dependencies
                    .dictionaries[name]
                    .words
            }
            .sortedByDescending(Word::weight),
        knowledgeRepository = dependencies.knowledgeRepository,
    )

    val question: StateFlow<PageModel> = skeleton
        .page
        .mapWithScope(scope) { questionScope, questionModelSkeleton ->
            PageModel(
                scope = questionScope,
                dependencies = dependencies.question(),
                skeleton = questionModelSkeleton,
                switchToNextQuestion = { keyToExclude ->
                    skeleton.page.value = PageModel.Skeleton(
                        wordToExclude = keyToExclude,
                    )
                },
                engine = engine,
            )
        }

    fun confirmGoBack() = goBack()

    fun cancelGoBack() {
        skeleton.displayConfirmGoBack.value = false
    }

    override val goBackHandler: StateFlow<(() -> Unit)?> = question
        .scopedInState(scope)
        .flatMapState(scope) { (modelScope, currentExerciseModel) ->
            currentExerciseModel.goBackHandler.mapState(modelScope) { goBack ->
                goBack.ifNull {
                    {
                        skeleton
                            .displayConfirmGoBack
                            .update { displayConfirmGoBack -> !displayConfirmGoBack }
                    }
                }
            }
        }
}