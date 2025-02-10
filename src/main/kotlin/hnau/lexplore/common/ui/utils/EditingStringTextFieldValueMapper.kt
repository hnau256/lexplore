package hnau.lexplore.common.ui.utils

/*private val editingStringTextFieldValueMapper = Mapper<EditingString, TextFieldValue>(
    direct = { editingString ->
        TextFieldValue(
            text = editingString.text,
            selection = TextRange(
                start = editingString.selection.first,
                end = editingString.selection.last,
            ),
        )
    },
    reverse = { textFieldValue ->
        EditingString(
            text = textFieldValue.text,
            selection = IntRange(
                start = textFieldValue.selection.min,
                endInclusive = textFieldValue.selection.max,
            ),
        )
    },
)

val EditingString.Companion.textFieldValueMapper: Mapper<EditingString, TextFieldValue>
    get() = editingStringTextFieldValueMapper*/
