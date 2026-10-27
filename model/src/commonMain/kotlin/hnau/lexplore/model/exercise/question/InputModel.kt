package hnau.lexplore.model.exercise.question

import hnau.lexplore.fsrs.dto.FsrsRating
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapState

class InputModel(
    scope: CoroutineScope,
    val input: MutableStateFlow<String>,
    private val onGradeSelected: (FsrsRating, String) -> Unit,
) {

    val hasInput: StateFlow<Boolean> = input.mapState(scope) { value ->
        value.trim().isNotEmpty()
    }

    fun onGrade(
        rating: FsrsRating,
    ) {
        onGradeSelected(rating, input.value)
    }

    @Pipe
    interface Dependencies
}
