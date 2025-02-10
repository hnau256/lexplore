package hnau.lexplore.common.ui.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun String.toTextFieldValue(): TextFieldValue = TextFieldValue(
    text = this,
    selection = TextRange(length),
)