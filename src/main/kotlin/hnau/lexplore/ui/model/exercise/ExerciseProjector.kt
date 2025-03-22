package hnau.lexplore.ui.model.exercise

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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
import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.ui.uikit.ScreenContent
import hnau.lexplore.common.ui.uikit.ScreenContentDependencies
import hnau.lexplore.common.ui.uikit.progressindicator.ProgressIndicatorPanel
import hnau.lexplore.common.ui.uikit.state.BooleanStateContent
import hnau.lexplore.common.ui.uikit.state.StateContent
import hnau.lexplore.common.ui.uikit.state.TransitionSpec
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.getTransitionSpecForHorizontalSlide
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.ui.model.exercise.question.QuestionProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.milliseconds

class ExerciseProjector(
    scope: CoroutineScope,
    private val model: ExerciseModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun question(): QuestionProjector.Dependencies

        fun screenContent(): ScreenContentDependencies
    }

    private val question: StateFlow<Loadable<Pair<WordToLearn, QuestionProjector>>> = model
        .question
        .mapWithScope(scope) { questionScope, questionModelOrLoading ->
            questionModelOrLoading.map { questionModel ->
                val wordToLearn = questionModel.wordToLearn
                val projector = QuestionProjector(
                    scope = questionScope,
                    model = questionModel,
                    dependencies = dependencies.question(),
                )
                wordToLearn to projector
            }
        }

    @Composable
    fun Content() {
        ScreenContent(
            dependencies = remember(dependencies) { dependencies.screenContent() },
            topAppBarContent = {
                Spacer(modifier = Modifier.weight(1f))
                Action(onClick = model.switchAutoTTS.collectAsState().value, content = {
                    Icon {
                        val autoTTS by model.autoTTS.active.collectAsState()
                        when (autoTTS) {
                            true -> VolumeUp
                            false -> VolumeOff
                        }
                    }
                })
            }
        ) { contentPadding ->
            question
                .StateContent(
                    modifier = Modifier
                        .fillMaxSize(),
                    transitionSpec = getTransitionSpecForHorizontalSlide(
                        duration = 300.milliseconds,
                        slideCoefficientProvider = { 0.3f },
                    ),
                    label = "ExerciseQuestion",
                    contentKey = { wordWithQuestionOrLoading ->
                        when (wordWithQuestionOrLoading) {
                            Loadable.Loading -> null
                            is Loadable.Ready -> wordWithQuestionOrLoading.value.first.word
                        }
                    }
                ) { wordWithQuestionOrLoading ->
                    wordWithQuestionOrLoading
                        .orNull()
                        ?.let { (_, question) ->
                            question.Content(
                                contentPadding = contentPadding,
                            )
                        }
                }
            model
                .isInProgress
                .BooleanStateContent(
                    transitionSpec = TransitionSpec.crossfade(),
                ) {
                    ProgressIndicatorPanel()
                }
            ConfirmGoBackDialog()
        }
    }

    @Composable
    private fun ConfirmGoBackDialog() {
        val displayConfirmGoBack by model.displayConfirmGoBack.collectAsState()
        if (!displayConfirmGoBack) {
            return
        }
        AlertDialog(
            title = { Text(stringResource(R.string.exercise_cancel_title)) },
            text = { Text(stringResource(R.string.exercise_cancel_text)) },
            confirmButton = {
                TextButton(
                    onClick = model::confirmGoBack,
                    content = { Text(stringResource(R.string.yes)) },
                )
            },
            dismissButton = {
                TextButton(
                    onClick = model::cancelGoBack,
                    content = { Text(stringResource(R.string.no)) },
                )
            },
            onDismissRequest = model::cancelGoBack
        )
    }
}