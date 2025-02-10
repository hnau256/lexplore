package hnau.lexplore.data.knowledge.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.WordToLearn
import kotlinx.datetime.Instant


@Entity(
    tableName = KnowledgeLevelOfWord.table,
)
data class KnowledgeLevelOfWord(

    @ColumnInfo(name = columnQuestion)
    @PrimaryKey
    val question: WordToLearn,

    @ColumnInfo(name = columnLastAnswerTimestamp)
    val lastAnswerTimestamp: Instant,

    @ColumnInfo(name = columnLevel)
    val level: ForgettingFactor,
) {

    companion object {

        const val table = "knowledge_level"

        const val columnQuestion = "question"
        const val columnLastAnswerTimestamp = "last_answer_timestamp"
        const val columnLevel = "level"
    }
}

