package hnau.lexplore.projector.exercise.question.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import hnau.lexplore.model.exercise.question.InputModel
import hnau.lexplore.model.exercise.question.nonEmptyEntries
import hnau.lexplore.projector.Localization
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SLine
import org.hnau.commons.app.projector.fractal.STabs
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.STextField
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.fractal.table.STable
import org.hnau.commons.app.projector.fractal.table.Subtable
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.app.projector.utils.collectAsMutableAccessor
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.fold

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

        SLine(
            acrossOrientation = Alignment.CenterHorizontally,
            orientation = Orientation.Vertical,
            separation = LocalDistance.current.units.padding.along.medium,
        ) {

            var selectedRating by model.selectedRating.collectAsMutableAccessor()
            STabs(
                items = InputModel.KnownFsrsRating.nonEmptyEntries,
                getSelection = { selectedRating },
                onSelectionChanged = { newRating -> selectedRating = newRating },
                importanceToActivate = null,
            ) { rating ->
                SText(
                    text = dependencies.localization.rating[rating.base]
                )
            }

            STable(
                orientation = Orientation.Vertical,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Subtable {
                    SCell(
                        modifier = Modifier.weight(1f),
                    ) {

                        val input by model.input.collectAsState()
                        val focusRequester = remember { FocusRequester() }

                        LaunchedEffect(focusRequester) {
                            focusRequester.requestFocus()
                        }

                        STextField(
                            value = input,
                            onValueChanged = { newValue -> model.input.value = newValue },
                            modifier = Modifier.focusRequester(focusRequester),
                            onKeyboardAction = KeyboardActionHandler {
                                model.enter.value?.fold(
                                    ifAction = { action -> action(Unit) },
                                    ifElse = {}
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                            ),
                            lineLimits = TextFieldLineLimits.SingleLine,
                        )
                    }
                    SCell {
                        SButton(
                            actionOrElseOrDisabled = model.enter.collectAsState().value,
                            titleOrIcon = TitleOrIcon.Icon(
                                Drawable.Vector(Icons.Default.Done),
                            )
                        )
                    }
                }
            }
        }
    }
}
