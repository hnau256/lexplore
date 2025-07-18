package hnau.lexplore.ui.model.exercise.question

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import hnau.lexplore.R
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.ui.uikit.ScreenContentDependencies
import hnau.lexplore.common.ui.uikit.Separator
import hnau.lexplore.common.ui.uikit.state.NullableStateContent
import hnau.lexplore.common.ui.uikit.state.StateContent
import hnau.lexplore.common.ui.uikit.state.TransitionSpec
import hnau.lexplore.common.ui.uikit.table.Table
import hnau.lexplore.common.ui.uikit.table.TableOrientation
import hnau.lexplore.common.ui.uikit.table.cellBox
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.lexplore.common.ui.utils.verticalDisplayPadding
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.knowLevel
import hnau.lexplore.ui.model.exercise.question.error.ErrorProjector
import hnau.lexplore.ui.model.exercise.question.input.InputProjector
import hnau.lexplore.ui.model.exercise.question.menu.MenuProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class QuestionProjector(
    scope: CoroutineScope,
    private val model: QuestionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun error(): ErrorProjector.Dependencies

        fun input(): InputProjector.Dependencies

        fun screenContent(): ScreenContentDependencies

        fun menu(): MenuProjector.Dependencies
    }

    private val state: StateFlow<QuestionStateProjector> =
        model.state.mapWithScope(scope) { stateScope, state ->
            when (state) {
                is QuestionStateModel.Error -> QuestionStateProjector.Error(
                    ErrorProjector(
                        scope = stateScope,
                        dependencies = dependencies.error(),
                        model = state.error,
                    )
                )

                is QuestionStateModel.Input -> QuestionStateProjector.Input(
                    InputProjector(
                        scope = stateScope,
                        dependencies = dependencies.input(),
                        model = state.input,
                    )
                )
            }
        }

    private val menu: StateFlow<MenuProjector?> =
        model.menu.mapWithScope(scope) { menuScope, menuOrNull ->
            menuOrNull?.let { menu ->
                MenuProjector(
                    scope = menuScope,
                    dependencies = dependencies.menu(),
                    model = menu,
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .verticalDisplayPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PreviousWordSpeaker()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = Dimens.smallSeparation,
                    alignment = Alignment.CenterHorizontally,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalDisplayPadding(),
            ) {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                IconButton(
                    onClick = model::switchMenuIsOpened,
                ) {
                    Icon(
                        modifier = Modifier.rotate(
                            degrees = animateFloatAsState(
                                when (menu.collectAsState().value) {
                                    null -> 0f
                                    else -> 90f
                                }
                            ).value,
                        )
                    ) { MoreVert }
                }
            }
            Separator()
            menu.NullableStateContent(
                modifier = Modifier.fillMaxWidth(),
                transitionSpec = TransitionSpec.vertical(),
            ) { menuProjector ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.separation)
                        .horizontalDisplayPadding(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                ) {
                    menuProjector.Content()
                }
            }
            model.info?.Content(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalDisplayPadding(),
            )
            Separator()
            Spacer(
                modifier = Modifier.weight(1f),
            )
            state.StateContent(
                contentKey = { localState ->
                    when (localState) {
                        is QuestionStateProjector.Input -> 0
                        is QuestionStateProjector.Error -> 1
                    }
                },
                label = "InputOrError",
                transitionSpec = TransitionSpec.vertical(),
            ) { localState ->
                localState.Content()
            }
        }
        AnimatedVisibility(
            visible = model.isAnswering.collectAsState().value,
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun PreviousWordSpeaker() {
        model.previousWordSpeaker.NullableStateContent(
            transitionSpec = TransitionSpec.vertical(),
        ) { previousWordSpeaker ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                Card(
                    shape = RoundedCornerShape(
                        topStartPercent = 0,
                        bottomStartPercent = 0,
                        topEndPercent = 100,
                        bottomEndPercent = 100,
                    ),
                    modifier = Modifier
                        .padding(
                            bottom = Dimens.separation,
                        )
                        .padding(
                            end = Dimens.separation,
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Separator(Dimens.smallSeparation)
                        Icon { ChevronLeft }
                        Text(
                            text = previousWordSpeaker.word.word,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        val speakOrNull: (() -> Unit)? by previousWordSpeaker.speak.collectAsState()
                        IconButton(
                            onClick = { speakOrNull?.invoke() },
                            enabled = speakOrNull != null,
                        ) {
                            Icon { VolumeUp }
                        }
                        IconButton(
                            onClick = previousWordSpeaker.close,
                        ) {
                            Icon { Close }
                        }
                    }
                }
            }
        }
    }
}