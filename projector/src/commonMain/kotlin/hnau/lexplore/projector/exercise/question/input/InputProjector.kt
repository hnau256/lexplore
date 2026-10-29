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
import hnau.lexplore.fsrs.dto.fold
import hnau.lexplore.model.exercise.question.InputModel
import org.hnau.commons.kotlin.ifTrue
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
import org.hnau.commons.app.projector.fractal.utils.Importance
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
        val input by model.input.collectAsState()
        val hasInput = input.trim().isNotEmpty()
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
                FsrsRating.gradeOrder.forEach { rating ->
                    GradeCell(
                        rating = rating,
                        enabled = rating.fold(
                            ifAgain = { true },
                            ifHard = { hasInput },
                            ifGood = { hasInput },
                            ifEasy = { hasInput },
                        ),
                    )
                }
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
                actionOrElseOrDisabled = enabled.ifTrue {
                    ActionOrElse.instant { model.onGrade(rating) }
                },
                titleOrIcon = TitleOrIcon.Title(
                    dependencies.localization.gradeLabel(rating),
                ),
                importanceToActivate = rating.fold(
                    ifAgain = { Importance.Secondary },
                    ifHard = { Importance.Secondary },
                    ifGood = { Importance.Primary },
                    ifEasy = { Importance.Secondary },
                ),
            )
        }
    }

}
