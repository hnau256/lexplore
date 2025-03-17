package hnau.lexplore.sentence.lang.noun

import hnau.lexplore.sentence.dimension.Gender
import hnau.lexplore.sentence.dimension.NumberValues
import hnau.lexplore.sentence.lang.russian.dimension.Animacy
import hnau.lexplore.sentence.lang.russian.dimension.RussianCaseValues

fun Noun.Russian.Companion.create(
    singular: RussianCaseValues<String>,
    plural: RussianCaseValues<String>,
    gender: Gender,
    animacy: Animacy = Animacy.Inanimate,
): Noun.Russian = Noun.Russian(
    gender = gender,
    animacy = animacy,
    values = NumberValues(
        singular = singular.toCaseValues(animacy),
        plural = plural.toCaseValues(animacy),
    ),
)

fun Noun.Russian.Companion.create(
    singularNominative: String,
    animacy: Animacy = Animacy.Inanimate,
): Noun.Russian {
    val info = NounInfo.create(singularNominative)
    val variants = info.buildVariants()
    return create(
        singular = variants.singular,
        plural = variants.plural,
        gender = info.type.gender,
        animacy = animacy,
    )
}

private sealed interface NounType {

    val gender: Gender

    fun buildVariants(
        base: String,
    ): NumberValues<RussianCaseValues<String>>

    sealed interface FirstDeclension : NounType {

        data object Masculine : FirstDeclension {

            override val gender: Gender
                get() = Gender.Masculine

            override fun buildVariants(
                base: String,
            ): NumberValues<RussianCaseValues<String>> = NumberValues(
                RussianCaseValues("${base}я", "${base}и"),
                RussianCaseValues("${base}и", "${base}ий)")
            )
        }

        data object Feminine : FirstDeclension {

            override val gender: Gender
                get() = Gender.Feminine

            override fun buildVariants(
                base: String,
            ): NumberValues<RussianCaseValues<String>> = NumberValues(
                RussianCaseValues("${base}а", "${base}ы"),
                RussianCaseValues("${base}ы", base),
            )
        }
    }

    sealed interface SecondDeclension : NounType {

        data object Masculine : SecondDeclension {

            override val gender: Gender
                get() = Gender.Masculine

            override fun buildVariants(
                base: String,
            ): NumberValues<RussianCaseValues<String>> = NumberValues(
                RussianCaseValues(base, "${base}а"),
                RussianCaseValues("${base}ы", "${base}ов"),
            )
        }

        data object Neuter : SecondDeclension {

            override val gender: Gender
                get() = Gender.Neuter

            override fun buildVariants(
                base: String,
            ): NumberValues<RussianCaseValues<String>> = NumberValues(
                RussianCaseValues("${base}о", "${base}а"),
                RussianCaseValues("${base}а", base),
            )
        }
    }

    data object ThirdDeclension : NounType {

        override val gender: Gender
            get() = Gender.Feminine

        override fun buildVariants(
            base: String,
        ): NumberValues<RussianCaseValues<String>> = NumberValues(
            RussianCaseValues("${base}ь", "${base}и"),
            RussianCaseValues("${base}и", "${base}ей"),
        )
    }
}

private data class NounInfo(
    val type: NounType,
    val base: String,
) {

    fun buildVariants(): NumberValues<RussianCaseValues<String>> = type.buildVariants(
        base = base,
    )

    companion object {

        private val consonants: Set<Char> = "бвгджзйклмнпрстфхцчшщ".toSet()

        fun create(
            singularNominative: String,
        ): NounInfo {
            val lastChar = singularNominative.last()
            val secondLastChar =
                if (singularNominative.length > 1) singularNominative[singularNominative.length - 2] else ' '

            return when {
                lastChar == 'я' && secondLastChar in "гкх" -> NounInfo(
                    type = NounType.FirstDeclension.Masculine,
                    base = singularNominative.dropLast(1)
                )

                lastChar == 'а' -> NounInfo(
                    type = NounType.FirstDeclension.Feminine,
                    base = singularNominative.dropLast(1),
                )

                lastChar == 'я' && secondLastChar !in "гкх" -> NounInfo(
                    type = NounType.FirstDeclension.Feminine,
                    base = singularNominative.dropLast(1)
                )

                lastChar in consonants -> NounInfo(
                    type = NounType.SecondDeclension.Masculine,
                    base = singularNominative
                )

                lastChar == 'о' -> NounInfo(
                    type = NounType.SecondDeclension.Neuter,
                    base = singularNominative.dropLast(1)
                )

                lastChar == 'е' -> NounInfo(
                    type = NounType.SecondDeclension.Neuter,
                    base = singularNominative.dropLast(1)
                )

                lastChar == 'ь' && secondLastChar in consonants -> NounInfo(
                    type = NounType.ThirdDeclension,
                    base = singularNominative.dropLast(1)
                )

                else -> error("Unable resolve noun info of '$singularNominative'")
            }
        }
    }
}