package hnau.lexplore.projector.exercise

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import hnau.lexplore.common.WordToLearn
import hnau.lexplore.model.exercise.ExerciseModel
import hnau.lexplore.projector.Localization
import hnau.lexplore.projector.exercise.question.QuestionPrepareProjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.size.SizeType
import org.hnau.commons.app.projector.uikit.progressindicator.ProgressIndicatorInBox
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.state.LoadableContent
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.uikit.transition.getTransitionSpecForSlide
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.foldBoolean

class ExerciseProjector(
    scope: CoroutineScope,
    private val model: ExerciseModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun question(): QuestionPrepareProjector.Dependencies
    }

    private val localization: Localization
        get() = dependencies.localization

    private val question: StateFlow<KeyValue<WordToLearn?, QuestionPrepareProjector>> = model
        .question
        .mapWithScope(scope) { scope, wordWithQuestion ->
            wordWithQuestion.map { question ->
                QuestionPrepareProjector(
                    scope = scope,
                    model = question,
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
                            autoTTS.foldBoolean(
                                ifTrue = { Icons.Default.VolumeUp },
                                ifFalse = { Icons.Default.VolumeOff },
                            )
                        )
                    ),
                )
            },
        ) {
            question
                .collectAsState()
                .value
                .StateContent(
                    contentKey = KeyValue<WordToLearn?, *>::key,
                    label = "Question",
                    transitionSpec = getTransitionSpecForSlide(
                        orientation = Orientation.Horizontal,
                        slideCoefficientProvider = { 1f },
                    ),
                ) { (_, question) ->
                    question.Content()
                }
        }
    }
}
