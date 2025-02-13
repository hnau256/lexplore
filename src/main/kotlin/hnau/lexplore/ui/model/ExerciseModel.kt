package hnau.lexplore.ui.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hnau.lexplore.R
import hnau.lexplore.common.kotlin.coroutines.flatMapState
import hnau.lexplore.common.kotlin.coroutines.mapState
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.coroutines.scopedInState
import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.uikit.ScreenContentDependencies
import hnau.lexplore.common.ui.utils.getTransitionSpecForHorizontalSlide
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.Engine
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

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

    private val displayConfirmGoBack: StateFlow<Boolean>
        get() = skeleton.displayConfirmGoBack

    private val engine = Engine(
        words = skeleton
            .dictionaries
            .flatMap { name ->
                dependencies
                    .dictionaries[name]
                    .words
            }
            .sortedBy(Word::index),
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

    @Shuffle
    interface ContentDependencies {

        fun question(): PageModel.ContentDependencies

        fun screenContent(): ScreenContentDependencies
    }

    @Composable
    fun Content(
        dependencies: ContentDependencies,
    ) {
        val currentQuestion by question.collectAsState()
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize(),
            targetState = currentQuestion,
            label = "CurrentQuestion",
            transitionSpec = getTransitionSpecForHorizontalSlide(
                duration = 400.milliseconds,
                slideCoefficientProvider = { 0.2f },
            )
        ) { localCurrentQuestion ->
            localCurrentQuestion.Content(
                dependencies = remember(dependencies) { dependencies.question() },
            )
        }
        ConfirmGoBackDialog()
    }

    @Composable
    private fun ConfirmGoBackDialog() {
        val displayConfirmGoBack by displayConfirmGoBack.collectAsState()
        if (!displayConfirmGoBack) {
            return
        }
        AlertDialog(
            title = { Text(stringResource(R.string.exercise_cancel_title)) },
            text = { Text(stringResource(R.string.exercise_cancel_text)) },
            confirmButton = {
                TextButton(
                    onClick = ::confirmGoBack,
                    content = { Text(stringResource(R.string.confirm)) },
                )
            },
            dismissButton = {
                TextButton(
                    onClick = ::cancelGoBack,
                    content = { Text(stringResource(R.string.cancel)) },
                )
            },
            onDismissRequest = ::cancelGoBack
        )
    }
}