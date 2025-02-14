package hnau.lexplore.ui.model.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import hnau.lexplore.R
import hnau.lexplore.common.ui.uikit.TextInput
import hnau.lexplore.common.ui.uikit.bubble.Bubble
import hnau.lexplore.common.ui.uikit.bubble.BubblesShower
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class EditWordProjector(
    private val scope: CoroutineScope,
    private val model : EditWordModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val bubblesShower: BubblesShower
    }

    @Composable
    fun Content() {val context = LocalContext.current
        val focusRequester = remember { FocusRequester() }
        TextInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.separation)
                .focusRequester(focusRequester),
            value = model.input,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions {
                val saveResult = model.save()
                if (!saveResult) {
                    dependencies.bubblesShower.showBubble(
                        Bubble(
                            text = context.getString(R.string.edit_incorrect_number)
                        )
                    )
                }
            }
        )
        LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    }
}