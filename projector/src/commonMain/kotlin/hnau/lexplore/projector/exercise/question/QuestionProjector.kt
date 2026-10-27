package hnau.lexplore.projector.exercise.question

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.lexplore.model.exercise.question.QuestionModel
import hnau.lexplore.model.exercise.question.QuestionStateModel
import hnau.lexplore.model.exercise.question.fold
import hnau.lexplore.projector.Localization
import hnau.lexplore.projector.exercise.question.error.ErrorProjector
import hnau.lexplore.projector.exercise.question.input.InputProjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SLine
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.size.SizeType
import org.hnau.commons.app.projector.fractal.table.STable
import org.hnau.commons.app.projector.fractal.table.STableHeader
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.table.lazy.item
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifNull
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.instant

class QuestionProjector(
    scope: CoroutineScope,
    private val model: QuestionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun input(): InputProjector.Dependencies

        fun error(): ErrorProjector.Dependencies
    }

    private val state: StateFlow<QuestionStateProjector> = model
        .state
        .mapWithScope(scope) { _, state ->
            state.fold(
                ifInput = { input ->
                    QuestionStateProjector.Input(
                        InputProjector(
                            model = input,
                            dependencies = dependencies.input(),
                        )
                    )
                },
                ifError = { error ->
                    QuestionStateProjector.Error(
                        ErrorProjector(
                            model = error,
                            dependencies = dependencies.error(),
                        )
                    )
                },
            )
        }

    @Composable
    fun Content() {
        val localization = dependencies.localization
        SLine(
            orientation = Orientation.Vertical,
            modifier = Modifier.fillMaxSize(),
        ) {
            SLazyTable(
                orientation = Orientation.Vertical,
                modifier = Modifier.weight(1f),
            ) {
                item {
                    PreviousWordSpeaker()
                }
                item {
                    STableHeader {
                        SText(
                            text = model.title,
                            type = SizeType.Large,
                        )
                    }
                }
                model.info.foldNullable(
                    ifNull = {},
                    ifNotNull = { info ->
                        item {
                            WordInfoContent(
                                info = info,
                                localization = localization,
                            )
                        }
                    },
                )
            }
            state.collectAsState().value.Content()
        }
    }

    @Composable
    private fun PreviousWordSpeaker() {
        val previousWordSpeaker = model
            .previousWordSpeaker
            .collectAsState()
            .value
            .ifNull { return }
        val speak by previousWordSpeaker.speak.collectAsState()
        STable(
            orientation = Orientation.Horizontal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SCell(
                modifier = Modifier.weight(1f),
            ) {
                SPanel {
                    SText(
                        text = previousWordSpeaker.word.word,
                        type = SizeType.Medium,
                    )
                }
            }
            SCell {
                SButton(
                    actionOrElseOrDisabled = speak,
                    titleOrIcon = TitleOrIcon.Icon(
                        Drawable.Vector(Icons.Default.VolumeUp),
                    ),
                )
            }
            SCell {
                SButton(
                    actionOrElseOrDisabled = ActionOrElse.instant(previousWordSpeaker.close),
                    titleOrIcon = TitleOrIcon.Icon(
                        Drawable.Vector(Icons.Default.Close),
                    ),
                )
            }
        }
    }
}
