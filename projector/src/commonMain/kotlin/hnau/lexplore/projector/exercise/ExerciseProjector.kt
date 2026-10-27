package hnau.lexplore.projector.exercise

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import hnau.lexplore.model.exercise.ExerciseModel
import hnau.lexplore.projector.Localization
import hnau.lexplore.projector.exercise.question.QuestionProjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.size.SizeType
import org.hnau.commons.app.projector.uikit.progressindicator.ProgressIndicatorInBox
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.map

class ExerciseProjector(
    scope: CoroutineScope,
    private val model: ExerciseModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun question(): QuestionProjector.Dependencies
    }

    private val localization: Localization
        get() = dependencies.localization

    private val question: StateFlow<Loadable<QuestionProjector>> = model
        .question
        .mapWithScope(scope) { questionScope, questionOrLoading ->
            questionOrLoading.map { questionModel ->
                QuestionProjector(
                    scope = questionScope,
                    model = questionModel,
                    dependencies = dependencies.question(),
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val switchAutoTTS by model.switchAutoTTS.collectAsState()
        val autoTTS by model.autoTTS.active.collectAsState()
        val isInProgress by model.isInProgress.collectAsState()

        SScreen(
            contentPadding = contentPadding,
            title = {
                SText(
                    text = localization.exerciseTitle,
                    type = SizeType.Large,
                )
            },
            actions = {
                Action(
                    actionOrElseOrDisabled = switchAutoTTS,
                    titleOrIcon = TitleOrIcon.Icon(
                        Drawable.Vector(
                            when (autoTTS) {
                                true -> Icons.Default.VolumeUp
                                false -> Icons.Default.VolumeOff
                            }
                        )
                    ),
                )
            },
        ) {
            question.collectAsState().value.LoadableContent(
                transitionSpec = TransitionSpec.rememberCrossfade(),
            ) { questionProjector ->
                questionProjector.Content()
            }
            isInProgress.BooleanStateContent(
                transitionSpec = TransitionSpec.rememberCrossfade(),
            ) {
                ProgressIndicatorInBox()
            }
        }
    }
}
