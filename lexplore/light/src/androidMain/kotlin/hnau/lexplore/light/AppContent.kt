package hnau.lexplore.light

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import hnau.common.compose.uikit.chip.Chip
import hnau.common.compose.uikit.chip.ChipSize
import hnau.common.compose.uikit.chip.ChipStyle
import hnau.common.compose.uikit.progressindicator.chipInProgressLeadingContent
import hnau.common.compose.uikit.row.ChipsRow
import hnau.common.compose.uikit.shape.HnauShape
import hnau.common.compose.uikit.shape.end
import hnau.common.compose.uikit.shape.inRow
import hnau.common.compose.uikit.shape.start
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.getTransitionSpecForHorizontalSlide
import hnau.common.kotlin.Mutable
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
                onResult = engine::onResult,
                autoTTS = autoTTS,
                switchAutoTTS = { autoTTS = !autoTTS },
            )
        }
    }
}

private val surenessList: List<Engine.Sureness> = Engine
    .Sureness
    .entries
    .sortedBy { sureness ->
        when (sureness) {
            Engine.Sureness.primary -> Engine.Sureness.entries.size
            else -> sureness.ordinal
        }
    }

@Composable
fun WordContent(
    word: WordWithTranslation,
    tts: TTS,
    onResult: (result: Engine.Result) -> Unit,
    autoTTS: Boolean,
    switchAutoTTS: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.largeSeparation)
            .verticalScroll(rememberScrollState()),
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
                onClick = { onResult(Engine.Result.Useless) },
                size = ChipSize.large,
                content = {
                    Text(
                        text = "Useless",
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


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(100)
                )
        ) {
            val knowledgeLevel = word.knowledgeLevel
            if (knowledgeLevel > 0) {
                Box(
                    modifier = Modifier
                        .weight(knowledgeLevel)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colors.primary,
                            shape = RoundedCornerShape(100)
                        )
                )
            }
            val unknowledgeLevel = 1f - knowledgeLevel
            if (unknowledgeLevel > 0) {
                Spacer(
                    modifier = Modifier.weight(unknowledgeLevel),
                )
            }
        }
        Text(
            text = word.russian,
            style = MaterialTheme.typography.h4,
        )
        Spacer(
            modifier = Modifier.weight(1f),
        )
        var incorrectWord: String? by remember { mutableStateOf(null) }
        var selectedSureness: Engine.Sureness by remember<Mutable<Engine.Sureness>> {
            Mutable(Engine.Sureness.primary)
        }
        AnimatedContent(
            targetState = incorrectWord,
            label = "KnownOrNot",
            contentKey = { it == null },
            modifier = Modifier.fillMaxWidth(),
        ) { localIncorrectWord ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
            ) {
                val onCorrect: () -> Unit = { onResult(Engine.Result.Correct(selectedSureness)) }
                when (localIncorrectWord) {

                    null -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                        ) {
                            var enteredWord: InputTextState by remember {
                                mutableStateOf(
                                    InputTextState(
                                        word = "",
                                        correct = false,
                                    )
                                )
                            }
                            TextToInput(
                                text = word.greek,
                                onChanged = { enteredWord = it },
                            )
                            ChipsRow(
                                all = surenessList,
                                modifier = Modifier.align(Alignment.End),
                            ) { sureness, shape ->
                                Chip(
                                    modifier = Modifier,
                                    content = { Text(sureness.name) },
                                    shape = shape,
                                    style = when (sureness) {
                                        Engine.Sureness.primary -> ChipStyle.button
                                        else -> ChipStyle.chip
                                    },
                                    onClick = {
                                        selectedSureness = sureness
                                        when (enteredWord.correct) {
                                            true -> onCorrect()
                                            false -> incorrectWord = enteredWord.word
                                        }
                                    },
                                )
                            }
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
                        ) {
                            Text(
                                text = "Incorrect: $incorrectWord",
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.chipsSeparation),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    text = "Typo",
                                    onClick = onCorrect,
                                    error = true,
                                    shape = HnauShape.inRow.start,
                                )
                                SpeakButton(
                                    word = word.greek,
                                    tts = tts,
                                    auto = autoTTS,
                                    shape = HnauShape.inRow.end,
                                )
                            }
                            Text(
                                text = word.greek,
                                style = MaterialTheme.typography.h4,
                            )
                            TextToInput(
                                text = word.greek,
                                onChanged = { (_, correct) ->
                                    if (correct) {
                                        onResult(Engine.Result.Incorrect)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class InputTextState(
    val word: String,
    val correct: Boolean,
)

@Composable
private fun TextToInput(
    text: String,
    onChanged: (InputTextState) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var enteredText: TextFieldValue by remember { mutableStateOf(TextFieldValue()) }
        val focusRequester = remember { FocusRequester() }
        val height = 52.dp
        OutlinedTextField(
            shape = HnauShape(),
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
                val word = newEnteredText.text.normalize
                onChanged(
                    InputTextState(
                        word = word,
                        correct = word == text.normalize,
                    )
                )
            },
            modifier = Modifier.focusRequester(focusRequester).weight(1f).height(height),
        )
        LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
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
    shape: Shape = HnauShape(),
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
        content = { Icon { RecordVoiceOver } },
        shape = shape,
    )
}