package hnau.lexplore.light

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.compose.uikit.chip.Chip
import hnau.common.compose.uikit.chip.ChipSize
import hnau.common.compose.uikit.chip.ChipStyle
import hnau.common.compose.uikit.progressindicator.chipInProgressLeadingContent
import hnau.common.compose.uikit.shape.HnauShape
import hnau.common.compose.uikit.shape.end
import hnau.common.compose.uikit.shape.inRow
import hnau.common.compose.uikit.shape.start
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.getTransitionSpecForHorizontalSlide
import hnau.lexplore.light.engine.Engine
import hnau.lexplore.light.engine.WordWithTranslation
import hnau.lexplore.light.ui.LexplorerTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AppContent(
    engine: Engine,
    tts: TTS,
) {
    LexplorerTheme {
        var autoTTS: Boolean by remember { mutableStateOf(true) }
        val currentWord: WordWithTranslation by engine.currentWord.collectAsState()
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = currentWord,
            label = "CurrentWord",
            transitionSpec = getTransitionSpecForHorizontalSlide(
                duration = 200.milliseconds,
            ) {
                0.1f
            }
        ) { localCurrentWord ->
            WordContent(
                word = localCurrentWord,
                tts = tts,
                onAnswer = engine::onAnswer,
                markAsKnown = engine::markAsKnown,
                autoTTS = autoTTS,
                switchAutoTTS = { autoTTS = !autoTTS },
            )
        }
    }
}

@Composable
fun WordContent(
    word: WordWithTranslation,
    tts: TTS,
    onAnswer: (isCorrect: Boolean) -> Unit,
    markAsKnown: () -> Unit,
    autoTTS: Boolean,
    switchAutoTTS: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.largeSeparation),
        verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = Dimens.separation,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            Chip(
                onClick = markAsKnown,
                size = ChipSize.large,
                content = {
                    Text(
                        text = "Known",
                    )
                },
                style = ChipStyle.chip,
            )
            Chip(
                onClick = switchAutoTTS,
                size = ChipSize.large,
                content = {
                    Text(
                        text = "Auto TTS",
                    )
                },
                style = when (autoTTS) {
                    true -> ChipStyle.chipSelected
                    false -> ChipStyle.chip
                },
            )
        }


        Text(
            text = word.word,
            style = MaterialTheme.typography.h4,
        )
        SpeakButton(
            word = word.word,
            tts = tts,
            auto = autoTTS,
        )
        Spacer(
            modifier = Modifier.weight(1f),
        )
        var state by remember { mutableStateOf(State.Default) }
        AnimatedContent(
            targetState = state,
            label = "state",
            contentAlignment = Alignment.Center,
        ) { localState ->
            when (localState) {

                State.Default -> DefaultState(
                    onKnown = { state = State.Known },
                    onUnknown = { state = State.Unknown },
                )

                State.Known -> KnownState(
                    word = word,
                    onAnswer = onAnswer,
                )

                State.Unknown -> UnknownState(
                    word = word,
                    onReady = { onAnswer(false) },
                )
            }
        }
    }
}

private enum class State { Default, Known, Unknown }

@Composable
private fun KnownState(
    word: WordWithTranslation,
    onAnswer: (isCorrect: Boolean) -> Unit,
) = Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(Dimens.separation),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Translation(
        word = word,
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(
            Dimens.chipsSeparation,
            Alignment.CenterHorizontally,
        )
    ) {
        Button(
            onClick = { onAnswer(true) },
            text = "Correct",
            shape = HnauShape.inRow.start,
        )
        Button(
            onClick = { onAnswer(false) },
            text = "Incorrect",
            error = true,
            shape = HnauShape.inRow.end,
        )
    }
}

@Composable
private fun DefaultState(
    onKnown: () -> Unit,
    onUnknown: () -> Unit,
) = Row(
    horizontalArrangement = Arrangement.spacedBy(
        Dimens.chipsSeparation,
        Alignment.CenterHorizontally,
    )
) {
    Button(
        onClick = onKnown,
        text = "Known",
        shape = HnauShape.inRow.start,
    )
    Button(
        onClick = onUnknown,
        text = "Unknown",
        error = true,
        shape = HnauShape.inRow.end,
    )
}

@Composable
private fun UnknownState(
    word: WordWithTranslation,
    onReady: () -> Unit,
) = Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(Dimens.separation),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Translation(
        word = word,
    )
    Button(
        onClick = onReady,
        text = "Ok",
    )
}

@Composable
private fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ChipStyle = ChipStyle.button,
    error: Boolean = false,
    shape: Shape = HnauShape(),
) = Chip(
    modifier = modifier,
    onClick = onClick,
    size = ChipSize.large,
    content = { Text(text = text) },
    activeColor = when (error) {
        true -> MaterialTheme.colors.error
        false -> MaterialTheme.colors.primary
    },
    style = style,
    shape = shape,
)

@Composable
private fun Translation(
    word: WordWithTranslation,
) {
    Text(
        text = word.translation,
        style = MaterialTheme.typography.h4,
    )
}

@Composable
private fun SpeakButton(
    word: String,
    tts: TTS,
    auto: Boolean,
) {
    var speakNumber by remember { mutableIntStateOf(0) }
    var isSpeaking by remember { mutableStateOf(false) }
    LaunchedEffect(auto, speakNumber, tts, word) {
        if (auto || speakNumber > 0) {
            isSpeaking = true
            tts.speek(word)
            isSpeaking = false
        }
    }
    Chip(
        style = ChipStyle.chipSelected,
        size = ChipSize.large,
        leading = chipInProgressLeadingContent(
            inProgress = isSpeaking,
        ),
        onClick = when (isSpeaking) {
            true -> null
            false -> {
                { speakNumber++ }
            }
        },
        content = { Icon { RecordVoiceOver } }
    )
}