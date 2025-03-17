package hnau.lexplore.sentence.lang.noun

import hnau.lexplore.sentence.dimension.CaseValues
import hnau.lexplore.sentence.dimension.Gender
import hnau.lexplore.sentence.dimension.NumberValues
import hnau.lexplore.sentence.lang.russian.dimension.Animacy
import kotlinx.serialization.Serializable

@Serializable
data class Noun(
    val greek: Greek,
    val russian: Russian,
) {

    constructor(
        greek: String,
        russian: String,
    ): this(
        greek = Greek.create(
            singularNominative = greek,
        ),
        russian = Russian.create(
            singularNominative = russian,
        )
    )

    @Serializable
    data class Greek(
        val gender: Gender,
        val values: NumberValues<CaseValues<String>>,
    ) {

        companion object
    }

    @Serializable
    data class Russian(
        val gender: Gender,
        val animacy: Animacy,
        val values: NumberValues<CaseValues<String>>,
    ) {

        companion object
    }

    companion object
}