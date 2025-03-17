package hnau.lexplore.sentence.lang.greek

import hnau.lexplore.sentence.dimension.CaseValues
import hnau.lexplore.sentence.dimension.GenderValues
import hnau.lexplore.sentence.dimension.NEndingValues
import hnau.lexplore.sentence.dimension.NumberValues

data object GreekArticle {

    val definite: NumberValues<GenderValues<CaseValues<NEndingValues<String>>>> =
        NumberValues(
            singular = GenderValues(
                masculine = CaseValues(
                    nominative = nEndingValues("ο"),
                    genitive = nEndingValues("του"),
                    accusative = nEndingValues("τον"),
                ),
                feminine = CaseValues(
                    nominative = nEndingValues("το"),
                    genitive = nEndingValues("του"),
                    accusative = nEndingValues("το"),
                ),
                neuter = CaseValues(
                    nominative = nEndingValues("η"),
                    genitive = nEndingValues("της"),
                    accusative = nEndingValues("την", "τη"),
                ),
            ),

            plural = GenderValues(
                masculine = CaseValues(
                    nominative = nEndingValues("οι"),
                    genitive = nEndingValues("των"),
                    accusative = nEndingValues("τους"),
                ),
                feminine = CaseValues(
                    nominative = nEndingValues("οι"),
                    genitive = nEndingValues("των"),
                    accusative = nEndingValues("τις"),
                ),
                neuter = CaseValues(
                    nominative = nEndingValues("τα"),
                    genitive = nEndingValues("των"),
                    accusative = nEndingValues("τα"),
                )
            )
        )

    val indefinite: GenderValues<CaseValues<NEndingValues<String>>> =
        GenderValues(
            masculine = CaseValues(
                nominative = nEndingValues("ένας"),
                genitive = nEndingValues("ενός"),
                accusative = nEndingValues("έναν"),
            ),
            feminine = CaseValues(
                nominative = nEndingValues("μια"),
                genitive = nEndingValues("μιας"),
                accusative = nEndingValues("μιαν", "μια"),
            ),
            neuter = CaseValues(
                nominative = nEndingValues("ένα"),
                genitive = nEndingValues("ενός"),
                accusative = nEndingValues("ένα"),
            ),
        )

    private fun <T> nEndingValues(
        withN: T,
        withoutN: T = withN,
    ): NEndingValues<T> = NEndingValues(
        withN = withN,
        withoutN = withoutN,
    )
}

val Greek.article: GreekArticle
    get() = GreekArticle