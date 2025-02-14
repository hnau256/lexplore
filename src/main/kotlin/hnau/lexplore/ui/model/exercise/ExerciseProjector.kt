package hnau.lexplore.ui.model.exercise

import androidx.compose.animation.AnimatedContent
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
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.ui.utils.getTransitionSpecForHorizontalSlide
import hnau.lexplore.ui.model.page.PageProjector
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

        fun page(): PageProjector.Dependencies
    }

    private val page: StateFlow<PageProjector> = model
        .question
        .mapWithScope(scope) { questionScope, model ->
            PageProjector(
                scope = questionScope,
                model = model,
                dependencies = dependencies.page(),
            )
        }

    @Composable
    fun Content() {
        val currentPage: PageProjector by page.collectAsState()
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize(),
            targetState = currentPage,
            label = "CurrentQuestion",
            transitionSpec = getTransitionSpecForHorizontalSlide(
                duration = 400.milliseconds,
                slideCoefficientProvider = { 0.2f },
            )
        ) { localPage ->
            localPage.Content()
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