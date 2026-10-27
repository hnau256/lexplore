package hnau.lexplore.projector

import androidx.compose.runtime.Immutable
import hnau.lexplore.fsrs.dto.FsrsRating
import hnau.lexplore.fsrs.dto.fold

@Immutable
data class Localization(
    val dictionariesTitle: String = "Словари",
    val start: String = "Начать",
    val all: String = "Все",
    val none: String = "Ни одного",
    val exerciseTitle: String = "Упражнение",
    val gradeAgain: String = "Не знаю",
    val gradeHard: String = "Сложно",
    val gradeGood: String = "Нормально",
    val gradeEasy: String = "Легко",
    val infoKnowledgeLevel: String = "Помню",
    val infoForgettingFactor: String = "Знаю",
    val infoDifficulty: String = "Сложность",
    val infoLastAsk: String = "Пауза",
    val lastAskDays: String = "д",
    val lastAskHours: String = "ч",
    val lastAskMinutes: String = "м",
    val lastAskSeconds: String = "с",
    val dictionarySubtitle: (known: Int, unknown: Int, total: Int) -> String =
        { known, unknown, total -> "$known (знаю) + $unknown (не знаю) = $total" },
    val lastAsk: (days: Long, hours: Long, minutes: Long, seconds: Long) -> String =
        { days, hours, minutes, seconds ->
            listOf(
                lastAskDays to days,
                lastAskHours to hours,
                lastAskMinutes to minutes,
                lastAskSeconds to seconds,
            )
                .dropWhile { (_, value) -> value <= 0 }
                .take(2)
                .ifEmpty { listOf(lastAskSeconds to seconds) }
                .joinToString(
                    separator = " ",
                ) { (suffix, value) -> value.toString() + suffix }
        },
) {

    fun gradeLabel(
        rating: FsrsRating,
    ): String = rating.fold(
        ifAgain = this::gradeAgain,
        ifHard = this::gradeHard,
        ifGood = this::gradeGood,
        ifEasy = this::gradeEasy,
    )

    companion object {

        val default = Localization()
    }
}
