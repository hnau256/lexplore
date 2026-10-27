package hnau.lexplore.projector.exercise.question.error

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import hnau.lexplore.fsrs.dto.FsrsRating
import hnau.lexplore.fsrs.dto.nonEmptyEntries
import hnau.lexplore.model.exercise.question.ErrorModel
import hnau.lexplore.projector.Localization
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SElements
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.STextField
import org.hnau.commons.app.projector.fractal.STabs
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.context.FContext
import org.hnau.commons.app.projector.fractal.size.SizeType
import org.hnau.commons.app.projector.fractal.table.STable
import org.hnau.commons.app.projector.fractal.utils.Mood
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe

class ErrorProjector(
    private val model: ErrorModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    @Composable
    fun Content() {
        val input by model.input.collectAsState()
        val selectedRating by model.selectedRating.collectAsState()
        val correctWordSpeaker by model.correctWordSpeaker.collectAsState()
        val focusRequester = remember { FocusRequester() }

        SElements(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SPanel(
                modifier = Modifier.fillMaxWidth(),
            ) {
                FContext(
                    update = {
                        copy(mood = Mood.Error)
                    },
                ) {
                    SText(
                        text = model.incorrectInput,
                        type = SizeType.Large,
                    )
                }
            }
            STable(
                orientation = Orientation.Horizontal,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SCell(
                    modifier = Modifier.weight(1f),
                ) {
                    SPanel {
                        FContext(
                            update = {
                                copy(mood = Mood.Active.default)
                            },
                        ) {
                            SText(
                                text = model.wordToLearn.word,
                                type = SizeType.Large,
                            )
                        }
                    }
                }
                SCell {
                    SButton(
                        actionOrElseOrDisabled = correctWordSpeaker,
                        titleOrIcon = TitleOrIcon.Icon(
                            Drawable.Vector(Icons.Default.VolumeUp),
                        ),
                    )
                }
            }
            SPanel(
                modifier = Modifier.fillMaxWidth(),
            ) {
                STextField(
                    value = input,
                    onValueChanged = { newValue -> model.input.value = newValue },
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.None,
                    ),
                    lineLimits = TextFieldLineLimits.SingleLine,
                )
            }
            STabs(
                modifier = Modifier.fillMaxWidth(),
                items = FsrsRating.nonEmptyEntries,
                getSelection = { selectedRating },
                onSelectionChanged = model::selectRating,
            ) { rating ->
                SText(
                    text = dependencies.localization.gradeLabel(rating),
                    type = SizeType.Small,
                    maxLines = 1,
                )
            }
        }

        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
        }
    }


}
