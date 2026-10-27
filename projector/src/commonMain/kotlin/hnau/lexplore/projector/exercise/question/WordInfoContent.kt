package hnau.lexplore.projector.exercise.question

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import hnau.lexplore.fsrs.dto.WordInfo
import hnau.lexplore.fsrs.difficultyFrom0To1
import hnau.lexplore.fsrs.durationFromLastAnswer
import hnau.lexplore.fsrs.retrievability
import hnau.lexplore.fsrs.stabilityFrom0To1
import hnau.lexplore.projector.Localization
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SProgress
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.context.FContext
import org.hnau.commons.app.projector.fractal.table.STable
import org.hnau.commons.app.projector.fractal.table.Subtable
import org.hnau.commons.app.projector.fractal.utils.Mood
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.utils.Orientation
import kotlin.time.Clock

@Composable
fun WordInfoContent(
    info: WordInfo,
    localization: Localization,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        Row(
            title = localization.infoKnowledgeLevel,
            value = Row.Value.Fraction(
                fraction = info.retrievability(Clock.System.now()).value.toFloat(),
            ),
        ),
        Row(
            title = localization.infoForgettingFactor,
            value = Row.Value.Fraction(
                fraction = info.stabilityFrom0To1,
            ),
        ),
        Row(
            title = localization.infoDifficulty,
            value = Row.Value.Fraction(
                fraction = info.difficultyFrom0To1,
                isNegative = true,
            ),
        ),
        Row(
            title = localization.infoLastAsk,
            value = Row.Value.Text(
                text = info.durationFromLastAnswer.let { duration ->
                    var seconds = duration.inWholeSeconds
                    var minutes = seconds / 60
                    seconds -= minutes * 60
                    var hours = minutes / 60
                    minutes -= hours * 60
                    val days = hours / 24
                    hours -= days * 24
                    localization.lastAsk(days, hours, minutes, seconds)
                },
            ),
        ),
    )

    STable(
        modifier = modifier.fillMaxWidth(),
        orientation = Orientation.Horizontal,
    ) {
        Subtable {
            rows.forEach { row ->
                SCell(
                    modifier = Modifier.height(rowHeight),
                ) {
                    SPanel(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        SText(row.title)
                    }
                }
            }
        }
        Subtable(
            modifier = Modifier.weight(1f),
        ) {
            rows.forEach { row ->
                SCell(
                    modifier = Modifier.height(rowHeight),
                ) {
                    SPanel(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        when (val value = row.value) {
                            is Row.Value.Fraction -> FContext(
                                update = {
                                    if (value.isNegative) {
                                        copy(mood = Mood.Error)
                                    } else {
                                        this
                                    }
                                },
                            ) {
                                SProgress(
                                    getFraction = { value.fraction },
                                    onFractionChanged = null,
                                )
                            }

                            is Row.Value.Text -> SText(value.text)
                        }
                    }
                }
            }
        }
    }
}

private val rowHeight = 48.dp

private data class Row(
    val title: String,
    val value: Value,
) {

    sealed interface Value {

        data class Fraction(
            val fraction: Float,
            val isNegative: Boolean = false,
        ) : Value

        data class Text(
            val text: String,
        ) : Value
    }
}
