package hnau.lexplore.ui.model.question

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.getOrInit
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.kotlin.toAccessor
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.uikit.ScreenContent
import hnau.lexplore.common.ui.uikit.ScreenContentDependencies
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.progressindicator.ProgressIndicatorPanel
import hnau.lexplore.common.ui.uikit.shape.HnauShape
import hnau.lexplore.common.ui.uikit.shape.end
import hnau.lexplore.common.ui.uikit.shape.inRow
import hnau.lexplore.common.ui.uikit.shape.start
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.lexplore.common.ui.utils.verticalDisplayPadding
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.Question
import hnau.lexplore.exercise.dto.Sureness
import hnau.lexplore.exercise.knowLevel
import hnau.lexplore.utils.normalized
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlin.math.round

class QuestionModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val question: Question,
    private val switchToNextQuestion: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val error: MutableStateFlow<ErrorModel.Skeleton?> = MutableStateFlow(null),
        var input: InputModel.Skeleton? = null,
    )

    @Shuffle
    interface Dependencies {

        fun input(): InputModel.Dependencies

        fun error(): ErrorModel.Dependencies
    }

    @Shuffle
    interface ContentDependencies {

        fun error(): ErrorModel.ContentDependencies

        fun input(): InputModel.ContentDependencies

        fun screenContent(): ScreenContentDependencies
    }

    private val onAnswerInProgressRegistry = InProgressRegistry()

    private val state: StateFlow<QuestionStateModel> = skeleton
        .error
        .mapWithScope(scope) { stateScope, errorSkeleton ->
            when (errorSkeleton) {
                null -> QuestionStateModel.Input(
                    InputModel(
                        scope = stateScope,
                        skeleton = skeleton::input
                            .toAccessor()
                            .getOrInit { InputModel.Skeleton() },
                        dependencies = dependencies.input(),
                        onReady = ::onInput,
                    )
                )

                else -> QuestionStateModel.Error(
                    ErrorModel(
                        scope = stateScope,
                        skeleton = errorSkeleton,
                        dependencies = dependencies.error(),
                        onEnteredCorrect = { onAnswer(Answer.Incorrect) },
                        onTypo = ::onCorrect,
                        wordToLearn = question.word.toLearn,
                    )
                )
            }
        }

    private fun onAnswer(
        answer: Answer,
    ) {
        scope.launch {
            onAnswerInProgressRegistry.executeRegistered {
                question.answer(answer)
                switchToNextQuestion()
            }
        }
    }

    private fun onInput(
        input: String,
        sureness: Sureness,
    ) {
        when (input.normalized) {
            question.word.toLearn.word.normalized -> onCorrect(
                sureness = sureness,
            )

            else -> skeleton.error.value = ErrorModel.Skeleton(
                incorrectInput = input,
                selectedSureness = sureness,
            )
        }
    }

    private fun onCorrect(
        sureness: Sureness,
    ) {
        onAnswer(
            Answer.Correct(sureness)
        )
    }

    private val title: String
        get() = question.word.translation.translation

    @Composable
    fun Content(
        dependencies: ContentDependencies,
    ) {
        ScreenContent(
            dependencies = remember(dependencies) { dependencies.screenContent() },
            topAppBarContent = {
                Spacer(modifier = Modifier.weight(1f))
                Action(
                    onClick = { onAnswer(Answer.AlmostKnown) },
                    content = { Icon { School } }
                )
                Action(
                    onClick = { onAnswer(Answer.Useless) },
                    content = { Icon { Delete } }
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalDisplayPadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title + " (" + question.info?.forgettingFactor?.factor?.let { round(it * 10) / 10 } + ")",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .horizontalDisplayPadding(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .horizontalDisplayPadding()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(100)
                        )
                ) {
                    val knowLevel = question.info.knowLevel.level
                    if (knowLevel > 0) {
                        Box(
                            modifier = Modifier
                                .weight(knowLevel)
                                .fillMaxHeight()
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(100)
                                )
                        )
                    }
                    if (knowLevel < 1f) {
                        Spacer(
                            modifier = Modifier.weight(1f - knowLevel),
                        )
                    }
                }
                Spacer(
                    modifier = Modifier.weight(1f),
                )
                val stateModel by state.collectAsState()
                AnimatedContent(
                    targetState = stateModel,
                    label = "InputOrError",
                    contentKey = { localState ->
                        when (localState) {
                            is QuestionStateModel.Input -> 0
                            is QuestionStateModel.Error -> 1
                        }
                    }
                ) { localState ->
                    when (localState) {
                        is QuestionStateModel.Error -> localState.error.Content(
                            dependencies = remember(dependencies) { dependencies.error() },
                        )

                        is QuestionStateModel.Input -> localState.input.Content(
                            dependencies = remember(dependencies) { dependencies.input() },
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = onAnswerInProgressRegistry.isProgress.collectAsState().value,
            ) {
                ProgressIndicatorPanel()
            }
        }
    }
}