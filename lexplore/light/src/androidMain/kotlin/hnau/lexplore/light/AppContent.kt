package hnau.lexplore.light

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.chip.Chip
import hnau.common.compose.uikit.chip.ChipSize
import hnau.common.compose.uikit.chip.ChipStyle
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.getTransitionSpecForHorizontalSlide
import hnau.lexplore.light.engine.Engine
import hnau.lexplore.light.ui.LexplorerTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AppContent(
    engine: Engine,
    translator: Translator,
    tts: TTS,
) {
    LexplorerTheme {
        val currentWord by engine.currentWord.collectAsState()
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
                translator = translator,
                tts = tts,
                onAnswer = engine::onAnswer,
                markAsKnown = engine::markAsKnown,
            )
        }
    }
}

@Composable
fun WordContent(
    word: String,
    translator: Translator,
    tts: TTS,
    onAnswer: (isCorrect: Boolean) -> Unit,
    markAsKnown: () -> Unit,
) {
    LaunchedEffect(tts, word) {
        tts.speek(word)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.largeSeparation),
        verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.h4,
        )
        val translationOrNull: String? by produceState<String?>(
            initialValue = null,
            key1 = word,
        ) {
            value = translator.translateGreekToRussian(word)
        }
        translationOrNull?.let { translation ->
            Text(
                text = translation,
                style = MaterialTheme.typography.h4,
            )
        }
        Spacer(
            modifier = Modifier.weight(1f),
        )
        Chip(
            onClick = markAsKnown,
            size = ChipSize.large,
            content = {
                Text(
                    text = "Mark as known",
                )
            },
            style = ChipStyle.chip,
        )
        Chip(
            onClick = { onAnswer(true) },
            size = ChipSize.large,
            content = {
                Text(
                    text = "Known",
                )
            },
            style = ChipStyle.button,
        )
        Chip(
            onClick = { onAnswer(false) },
            size = ChipSize.large,
            content = {
                Text(
                    text = "Unknown",
                )
            },
            activeColor = MaterialTheme.colors.error,
            style = ChipStyle.button,
        )
    }
}