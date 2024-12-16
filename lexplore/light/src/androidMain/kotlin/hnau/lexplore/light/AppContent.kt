package hnau.lexplore.light

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.key
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
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.getTransitionSpecForHorizontalSlide
import hnau.lexplore.light.engine.Engine
import hnau.lexplore.light.engine.LearningWord
import hnau.lexplore.light.ui.LexplorerTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AppContent(
    engine: Engine,
    tts: TTS,
) {
    LexplorerTheme {
        var autoTTS: Boolean by remember { mutableStateOf(true) }
        val learningWord: LearningWord by engine.currentWord.collectAsState()
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = learningWord,
            contentKey = { localLearningWord ->
                localLearningWord.words[localLearningWord.correctWordIndex]
            },
            label = "CurrentWord",
            transitionSpec = getTransitionSpecForHorizontalSlide(
                duration = 200.milliseconds,
            ) {
                0.1f
            }
        ) { localLearningWord ->
            WordContent(
                word = localLearningWord,
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
    word: LearningWord,
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

        when (word.isNew) {
            true -> ContentUnknown(
                word = word,
                onReady = remember(onAnswer) { { onAnswer(false) } },
                tts = tts,
                autoTTS = autoTTS,
            )

            false -> ContentKnown(
                word = word,
                onAnswer = onAnswer,
                tts = tts,
                autoTTS = autoTTS,
            )
        }
    }
}

@Composable
private fun ColumnScope.ContentKnown(
    word: LearningWord,
    onAnswer: (isCorrect: Boolean) -> Unit,
    tts: TTS,
    autoTTS: Boolean,
) {
    Text(
        text = word.translation,
        style = MaterialTheme.typography.h4,
    )
    Spacer(
        modifier = Modifier.weight(1f),
    )
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    word.words.forEachIndexed { i, greekWord ->
        key(greekWord) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = when (selectedIndex) {
                    null -> {
                        when (i) {
                            word.correctWordIndex -> {
                                { onAnswer(true) }
                            }

                            else -> {
                                { selectedIndex = i }
                            }
                        }
                    }

                    else -> null
                },
                text = word.words[i],
                style = when {
                    selectedIndex != null && (i == selectedIndex || i == word.correctWordIndex) -> ChipStyle.button
                    else -> ChipStyle.chip
                },
                error = selectedIndex == i
            )
        }
    }
    AnimatedVisibility(
        visible = selectedIndex != null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            SpeakButton(
                tts = tts,
                word = word.words[word.correctWordIndex],
                auto = autoTTS,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAnswer(false) },
                text = "Next",
            )
        }
    }
}

@Composable
private fun ColumnScope.ContentUnknown(
    word: LearningWord,
    onReady: () -> Unit,
    tts: TTS,
    autoTTS: Boolean,
) {
    val greekWord = word.words[word.correctWordIndex]
    Text(
        text = greekWord,
        style = MaterialTheme.typography.h4,
    )
    SpeakButton(
        tts = tts,
        word = greekWord,
        auto = autoTTS,
    )
    Text(
        text = word.translation,
        style = MaterialTheme.typography.h4,
    )
    Spacer(
        modifier = Modifier.weight(1f),
    )
    Button(
        modifier = Modifier.fillMaxWidth(),
        text = "Ok",
        onClick = onReady,
    )
}

@Composable
private fun Button(
    text: String,
    onClick: (() -> Unit)?,
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