package hnau.lexplore.sentence.lang.russian

import hnau.lexplore.sentence.dimension.GenderValues
import hnau.lexplore.sentence.dimension.NumberValues
import hnau.lexplore.sentence.lang.russian.dimension.RussianCaseValues

object RussianArticle {

    val definite: NumberValues<GenderValues<RussianCaseValues<String>>> =
        NumberValues(
            singular = GenderValues(
                masculine = RussianCaseValues(
                    nominative = "этот",
                    genitive = "этого",
                ),
                feminine = RussianCaseValues(
                    nominative = "эта",
                    genitive = "этой",
                ),
                neuter = RussianCaseValues(
                    nominative = "это",
                    genitive = "этого",
                ),
            ),
            plural = GenderValues(
                all = RussianCaseValues(
                    nominative = "эти",
                    genitive = "этих",
                ),
            )
        )

    val indefinite: NumberValues<GenderValues<RussianCaseValues<String>>> =
        NumberValues(
            singular = GenderValues(
                masculine = RussianCaseValues(
                    nominative = "некий",
                    genitive = "некоего",
                ),
                feminine = RussianCaseValues(
                    nominative = "некая",
                    genitive = "некоей",
                ),
                neuter = RussianCaseValues(
                    nominative = "некое",
                    genitive = "некоего",
                ),
            ),
            plural = GenderValues(
                all = RussianCaseValues(
                    nominative = "некие",
                    genitive = "неких",
                ),
            )
        )
}

val Russian.article: RussianArticle
    get() = RussianArticle