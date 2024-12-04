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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.chip.Chip
import hnau.common.compose.uikit.chip.ChipSize
import hnau.common.compose.uikit.chip.ChipStyle
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.getTransitionSpecForHorizontalSlide
import hnau.lexplore.light.ui.LexplorerTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AppContent(
    words: List<Word>,
    translator: Translator,
    tts: TTS,
) {
    LexplorerTheme {
        var currentWord: Int by remember { mutableIntStateOf(0) }
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
                word = words[localCurrentWord].word,
                onReady = { currentWord++ },
                translator = translator,
                tts = tts,
            )
        }
    }
}

@Composable
fun WordContent(
    word: String,
    translator: Translator,
    tts: TTS,
    onReady: () -> Unit,
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
        translationOrNull?.let {translation ->
            Text(
                text = translation,
                style = MaterialTheme.typography.h4,
            )
        }
        Spacer(
            modifier = Modifier.weight(1f),
        )
        Chip(
            onClick = onReady,
            size = ChipSize.large,
            content = {
                Text(
                    text = "Next",
                )
            },
            style = ChipStyle.button,
        )
    }
}