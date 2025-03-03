package hnau.lexplore.ui.model.exercise

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hnau.lexplore.R
import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.ui.uikit.progressindicator.ProgressIndicatorPanel
import hnau.lexplore.common.ui.utils.getTransitionSpecForHorizontalSlide
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.ui.model.question.QuestionProjector
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
        val currentPage by question.collectAsState()
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize(),
            targetState = currentPage,
            label = "CurrentQuestion",
            transitionSpec = getTransitionSpecForHorizontalSlide(
                duration = 300.milliseconds,
                slideCoefficientProvider = { 0.3f },
            ),
            contentKey = { it.orNull()?.first?.word },
        ) { localQuestionOrLoading ->
            localQuestionOrLoading.fold(
                ifLoading = { },
                ifReady = { (_, question) -> question.Content() }
            )
        }
        AnimatedVisibility(
            visible = model.isInProgress.collectAsState().value,
        ) {
            ProgressIndicatorPanel()
        }
        ConfirmGoBackDialog()
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
                    content = { Text(stringResource(R.string.confirm)) },
                )
            },
            dismissButton = {
                TextButton(
                    onClick = model::cancelGoBack,
                    content = { Text(stringResource(R.string.cancel)) },
                )
            },
            onDismissRequest = model::cancelGoBack
        )
    }
}