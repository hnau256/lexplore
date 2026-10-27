package hnau.lexplore.projector.exercise.question.input

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
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
import hnau.lexplore.model.exercise.question.InputModel
import hnau.lexplore.projector.Localization
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.STextField
import org.hnau.commons.app.projector.fractal.table.STable
import org.hnau.commons.app.projector.fractal.table.STableScope
import org.hnau.commons.app.projector.fractal.table.Subtable
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.instant

class InputProjector(
    private val model: InputModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization
    }

    @Composable
    fun Content() {
        val hasInput by model.hasInput.collectAsState()
        val input by model.input.collectAsState()
        val focusRequester = remember { FocusRequester() }

        STable(
            orientation = Orientation.Vertical,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SCell {
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
            }
            Subtable {
                GradeCell(
                    rating = FsrsRating.Again,
                    enabled = true,
                )
                GradeCell(
                    rating = FsrsRating.Hard,
                    enabled = hasInput,
                )
                GradeCell(
                    rating = FsrsRating.Good,
                    enabled = hasInput,
                )
                GradeCell(
                    rating = FsrsRating.Easy,
                    enabled = hasInput,
                )
            }
        }

        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
        }
    }

    @Composable
    private fun STableScope.GradeCell(
        rating: FsrsRating,
        enabled: Boolean,
    ) {
        SCell(
            modifier = Modifier.weight(1f),
        ) {
            SButton(
                actionOrElseOrDisabled = when (enabled) {
                    true -> ActionOrElse.instant { model.onGrade(rating) }
                    false -> null
                },
                titleOrIcon = TitleOrIcon.Title(
                    dependencies.localization.gradeLabel(rating),
                ),
            )
        }
    }

}
