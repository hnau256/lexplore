package hnau.lexplore.light

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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
                duration = 400.milliseconds,
                slideCoefficientProvider = { 0.2f },
            )
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
            text = word.russian,
            style = MaterialTheme.typography.h4,
        )
        Spacer(
            modifier = Modifier.weight(1f),
        )
        var isError: Boolean by remember { mutableStateOf(false) }
        AnimatedContent(
            targetState = isError,
            label = "KnownOrNot",
            modifier = Modifier.fillMaxWidth(),
        ) { localIsError ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
            ) {
                when (localIsError) {
                    true -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.separation),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = word.greek,
                                style = MaterialTheme.typography.h4,
                                modifier = Modifier.weight(1f),
                            )
                            SpeakButton(
                                word = word.greek,
                                tts = tts,
                                auto = autoTTS,
                            )
                        }
                        TextToInput(
                            text = word.greek,
                            onAnswer = { onAnswer(false) },
                            auto = true,
                        )
                    }

                    false -> {
                        TextToInput(
                            text = word.greek,
                            onAnswer = { correct ->
                                when (correct) {
                                    false -> isError = true
                                    true -> onAnswer(true)
                                }
                            },
                            auto = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextToInput(
    text: String,
    onAnswer: (isCorrect: Boolean) -> Unit,
    auto: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var enteredText: TextFieldValue by remember { mutableStateOf(TextFieldValue()) }
        val focusRequester = remember { FocusRequester() }
        val isEnteredCorrect: () -> Boolean = { enteredText.text.normalize == text.normalize }
        val check: () -> Unit = { onAnswer(isEnteredCorrect()) }
        val height = 52.dp
        OutlinedTextField(
            shape = HnauShape.inRow.start,
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                imeAction = ImeAction.None,
                keyboardType = KeyboardType.Text,
            ),
            keyboardActions = KeyboardActions {},
            singleLine = true,
            value = enteredText,
            onValueChange = { newEnteredText ->
                enteredText = newEnteredText
                if (auto && isEnteredCorrect()) {
                    onAnswer(true)
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1f)
                .height(height),
        )
        LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
        if (!auto) {
            Button(
                style = ChipStyle.button,
                text = "Ok",
                onClick = check,
                shape = HnauShape.inRow.end,
                size = ChipSize.large.copy(
                    height = height,
                )
            )
        }
    }
}

private val String.normalize: String
    get() = lowercase().trim().withoutNonSpacingMarks

@Composable
private fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ChipStyle = ChipStyle.button,
    error: Boolean = false,
    shape: Shape = HnauShape(),
    size: ChipSize = ChipSize.large,
) = Chip(
    modifier = modifier,
    onClick = onClick,
    size = size,
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
        text = word.russian,
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