package hnau.lexplore.ui.model.exercise.question

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import hnau.lexplore.R
import hnau.lexplore.common.ui.uikit.table.Table
import hnau.lexplore.common.ui.uikit.table.TableOrientation
import hnau.lexplore.common.ui.uikit.table.cellBox
import hnau.lexplore.common.ui.uikit.table.subtable
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.durationFromLastAnswer
import hnau.lexplore.exercise.forgettingFactorFrom0To1
import hnau.lexplore.exercise.knowLevel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WordInfo.Content(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val rows = remember(this) {
        generateRows(
            context = context,
        )
    }
    Table(
        modifier = modifier,
        orientation = TableOrientation.Horizontal,
    ) {
        subtable {
            rows.forEach { (title) ->
                cellBox(
                    modifier = Modifier.height(rowHeight),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        modifier = Modifier.padding(
                            horizontal = Dimens.separation,
                        ),
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
        subtable {
            rows.forEach { (_, value) ->
                cellBox(
                    modifier = Modifier
                        .height(rowHeight),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    when (value) {
                        is Row.Value.Fraction -> LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.smallSeparation)
                                .padding(
                                    horizontal = Dimens.separation,
                                ),
                            progress = { value.fraction },
                            drawStopIndicator = {},
                        )

                        is Row.Value.Text -> Text(
                            modifier = Modifier
                                .padding(
                                    horizontal = Dimens.separation,
                                ),
                            text = value.text,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                }
            }
        }
    }
}

private val rowHeight = 48.dp

private fun WordInfo.generateRows(
    context: Context,
): ImmutableList<Row> = persistentListOf(
    Row(
        title = context.getString(R.string.question_info_knowledge_level),
        value = Row.Value.Fraction(knowLevel.level),
    ),
    Row(
        title = context.getString(R.string.question_info_forgetting_factor),
        value = Row.Value.Fraction(forgettingFactorFrom0To1),
    ),
    Row(
        title = context.getString(R.string.question_info_last_ask),
        value = Row.Value.Text(
            text = run {
                var seconds = durationFromLastAnswer.inWholeSeconds
                var minutes = seconds / 60
                seconds -= minutes * 60
                var hours = minutes / 60
                minutes -= hours * 60
                val days = hours / 24
                hours -= days * 24
                listOf(
                    R.string.question_info_last_ask_days to days,
                    R.string.question_info_last_ask_hours to hours,
                    R.string.question_info_last_ask_minutes to minutes,
                    R.string.question_info_last_ask_seconds to seconds,
                )
                    .dropWhile { (_, value) -> value <= 0 }
                    .take(2)
                    .joinToString(
                        separator = " ",
                    ) { (key, value) ->
                        value.toString() + context.getString(key)
                    }
            }
        ),
    ),
)


private data class Row(
    val title: String,
    val value: Value,
) {

    sealed interface Value {

        data class Fraction(
            val fraction: Float,
        ) : Value

        data class Text(
            val text: String,
        ) : Value
    }
}