package hnau.lexplore.ui.model.question

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.ui.uikit.ScreenContent
import hnau.lexplore.common.ui.uikit.ScreenContentDependencies
import hnau.lexplore.common.ui.uikit.progressindicator.ProgressIndicatorPanel
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.lexplore.common.ui.utils.verticalDisplayPadding
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.knowLevel
import hnau.lexplore.ui.model.error.ErrorProjector
import hnau.lexplore.ui.model.input.InputProjector
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.round

class QuestionProjector(
    scope: CoroutineScope,
    private val model: QuestionModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {


        fun error(): ErrorProjector.Dependencies

        fun input(): InputProjector.Dependencies

        fun screenContent(): ScreenContentDependencies
    }

    private val state: StateFlow<QuestionStateProjector> = model
        .state
        .mapWithScope(scope) { stateScope, state ->
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

    @Composable
    fun Content() {
        ScreenContent(
            dependencies = remember(dependencies) { dependencies.screenContent() },
            topAppBarContent = {
                Spacer(modifier = Modifier.weight(1f))
                Action(
                    onClick = { model.onAnswer(Answer.AlmostKnown) },
                    content = { Icon { School } }
                )
                Action(
                    onClick = { model.onAnswer(Answer.Useless) },
                    content = { Icon { Delete } }
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalDisplayPadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = model.title + " (" + model.question.info?.forgettingFactor?.factor?.let {
                        round(
                            it * 10
                        ) / 10
                    } + ")",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .horizontalDisplayPadding(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .horizontalDisplayPadding()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(100)
                        )
                ) {
                    val knowLevel = model.question.info.knowLevel.level
                    if (knowLevel > 0) {
                        Box(
                            modifier = Modifier
                                .weight(knowLevel)
                                .fillMaxHeight()
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(100)
                                )
                        )
                    }
                    if (knowLevel < 1f) {
                        Spacer(
                            modifier = Modifier.weight(1f - knowLevel),
                        )
                    }
                }
                Spacer(
                    modifier = Modifier.weight(1f),
                )
                AnimatedContent(
                    targetState = state.collectAsState().value,
                    label = "InputOrError",
                    contentKey = { localState ->
                        when (localState) {
                            is QuestionStateProjector.Input -> 0
                            is QuestionStateProjector.Error -> 1
                        }
                    }
                ) { localState ->
                    localState.Content()
                }
            }
            AnimatedVisibility(
                visible = model.isAnswering.collectAsState().value,
            ) {
                ProgressIndicatorPanel()
            }
        }
    }
}