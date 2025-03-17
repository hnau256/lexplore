package hnau.lexplore.sentence.lang.noun

import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.sentence.dimension.Case
import hnau.lexplore.sentence.dimension.CaseValues
import hnau.lexplore.sentence.dimension.Gender
import hnau.lexplore.sentence.dimension.GenderValues
import hnau.lexplore.sentence.dimension.Number
import hnau.lexplore.sentence.dimension.NumberValues
import hnau.lexplore.utils.normalized

private val greekEndings = GenderValues(
    masculine = listOf(
        NumberValues(
            singular = CaseValues("ος", "ου", "ο"),
            plural = CaseValues("οι", "ων", "ους"),
        ),
        NumberValues(
            singular = CaseValues("ης", "η", "η"),
            plural = CaseValues("ες", "ων", "ες"),
        ),
        NumberValues(
            singular = CaseValues("ας", "α", "α"),
            plural = CaseValues("ες", "ων", "ες"),
        ),
        NumberValues(
            singular = CaseValues("έας", "έα", "έα"),
            plural = CaseValues("είς", "έων", "είς"),
        ),
        NumberValues(
            singular = CaseValues("άς", "ά", "ά"),
            plural = CaseValues("άδες", "άδων", "άδες"),
        ),
    ),
    feminine = listOf(
        NumberValues(
            singular = CaseValues("η", "ης", "η"),
            plural = CaseValues("ες", "ών", "ες"),
        ),
        NumberValues(
            singular = CaseValues("α", "ας", "α"),
            plural = CaseValues("ες", "ών", "ες"),
        ),
        NumberValues(
            singular = CaseValues("ος", "ου", "ο"),
            plural = CaseValues("οι", "ων", "ους"),
        ),
    ),
    neuter = listOf(
        NumberValues(
            singular = CaseValues("ο", "ους", "ο"),
            plural = CaseValues("α", "ων", "α"),
        ),
        NumberValues(
            singular = CaseValues("ί", "ιού", "ί"),
            plural = CaseValues("ιά", "ιών", "ιά"),
        ),
        NumberValues(
            singular = CaseValues("μα", "ματος", "μα"),
            plural = CaseValues("ματα", "μάτων", "ματα"),
        ),
        NumberValues(
            singular = CaseValues("ος", "ους", "ος"),
            plural = CaseValues("η", "ών", "η"),
        ),
        NumberValues(
            singular = CaseValues("μο", "ματος", "μο"),
            plural = CaseValues("ματα", "μάτων", "ματα"),
        ),
    ),
).let { endings ->
    Gender
        .entries
        .flatMap { gender ->
            endings[gender].map { endings ->
                val singularNominativeEnding = endings[Number.Singular][Case.Nominative]
                Triple(
                    singularNominativeEnding,
                    gender,
                    endings,
                )
            }
        }
        .groupBy { it.first }
        .mapValues { (_, endingVariants) ->
            endingVariants
                .associate { (_, gender, endings) ->
                    gender to endings
                }
        }
        .toList()
        .sortedByDescending { it.first.length }
}

fun Noun.Greek.Companion.create(
    singularNominative: String,
    forceGender: Gender? = null,
): Noun.Greek {
    val endingVariants = greekEndings
        .find { (ending) -> singularNominative.endsWith(ending) }
        .ifNull {
            greekEndings
                .find { (ending) -> singularNominative.normalized.endsWith(ending.normalized) }
        }
        .ifNull { error("Unknown ending of '$singularNominative'") }
        .second
    val (gender, endings) = when (forceGender) {
        null -> endingVariants
            .toList()
            .minBy { (gender) -> gender.ordinal }
            .ifNull { error("Unknown ending of '$singularNominative'") }

        else -> {
            val endings = endingVariants[forceGender]
                .ifNull { error("Unknown endings for forced gender $forceGender for '$singularNominative'") }
            forceGender to endings
        }
    }
    val singularNominativeWithoutEnding = singularNominative
        .removeSuffix(endings[Number.Singular][Case.Nominative])

    return Noun.Greek(
        gender = gender,
        values = endings.map { cases ->
            cases.map { ending ->
                singularNominativeWithoutEnding + ending
            }
        }
    )
}