package hnau.lexplore.projector.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hnau.lexplore.data.api.dictionary.dto.Language

@Composable
fun Language.Content() {
    Box(
        modifier = Modifier.size(48.dp).background(Color.Magenta),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = flag ?: "",
            style = MaterialTheme.typography.h4,
        )
    }
}

val Language.flag: String?
    get() = locale?.country?.let(::getFlagEmoji)

private fun getFlagEmoji(
    countryCode: String,
): String? {
    val minLetter = 'A'.code
    val maxLetter = 'Z'.code
    val expectedCharsCount = 2
    val offset = 0x1F1E6 - minLetter
    return countryCode
        .takeIf { it.length == expectedCharsCount }
        ?.uppercase()
        ?.let { code ->
            (0 until expectedCharsCount).map { index -> code[index].code }
        }
        ?.takeIf { letters ->
            letters.all { letter -> letter in minLetter..maxLetter }
        }
        ?.fold(
            initial = CharArray(0),
        ) { acc, countryLetterCode ->
            acc + Character.toChars(countryLetterCode + offset)
        }
        ?.let(::String)
}