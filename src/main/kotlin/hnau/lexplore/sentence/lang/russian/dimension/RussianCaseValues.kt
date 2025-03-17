package hnau.lexplore.sentence.lang.russian.dimension

import hnau.lexplore.sentence.dimension.CaseValues

class RussianCaseValues<out T>(
    val nominative: T,
    val genitive: T,
    val accusative: Accusative<T> = Accusative.BasedOnAnimacy,
) {

    sealed interface Accusative<out T> {

        data class Custom<out T>(
            val value: T,
        ) : Accusative<T>

        data object BasedOnAnimacy : Accusative<Nothing>
    }

    fun toCaseValues(
        animacy: Animacy,
    ): CaseValues<T> = CaseValues(
        nominative = nominative,
        genitive = genitive,
        accusative = when (accusative) {
            Accusative.BasedOnAnimacy -> when (animacy) {
                Animacy.Animate -> genitive
                Animacy.Inanimate -> nominative
            }

            is Accusative.Custom<T> -> accusative.value
        }
    )

}