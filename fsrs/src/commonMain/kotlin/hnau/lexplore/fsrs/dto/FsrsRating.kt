package hnau.lexplore.fsrs.dto

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.enumvalues.annotations.EnumValues
import org.hnau.commons.gen.fold.annotations.Fold

@Fold(
    nonEmptyEntries = true,
)
@EnumValues(serializable = true)
@Serializable
enum class FsrsRating(
    val grade: Int,
) {
    Again(1),
    Hard(2),
    Good(3),
    Easy(4);

    companion object {

        val gradeOrder: NonEmptyList<FsrsRating> = nonEmptyListOf(
            Again,
            Hard,
            Easy,
            Good,
        )
    }
}
