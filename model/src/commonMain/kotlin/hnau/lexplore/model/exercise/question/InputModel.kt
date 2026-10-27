package hnau.lexplore.model.exercise.question

import hnau.lexplore.fsrs.dto.FsrsRating
import kotlinx.coroutines.flow.MutableStateFlow
import org.hnau.commons.gen.pipe.annotations.Pipe

class InputModel(
    val input: MutableStateFlow<String>,
    private val onGradeSelected: (FsrsRating, String) -> Unit,
) {

    fun onGrade(
        rating: FsrsRating,
    ) {
        onGradeSelected(rating, input.value)
    }

    @Pipe
    interface Dependencies
}
