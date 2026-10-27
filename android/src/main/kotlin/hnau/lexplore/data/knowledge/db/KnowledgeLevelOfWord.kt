package hnau.lexplore.data.knowledge.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hnau.lexplore.common.WordToLearn
import kotlin.time.Instant

@Entity(
    tableName = KnowledgeLevelOfWord.table,
)
data class KnowledgeLevelOfWord(

    @ColumnInfo(name = columnQuestion)
    @PrimaryKey
    val question: WordToLearn,

    @ColumnInfo(name = columnLastAnswerTimestamp)
    val lastAnswerTimestamp: Instant,

    @ColumnInfo(name = columnStability)
    val stability: Double,

    @ColumnInfo(name = columnDifficulty)
    val difficulty: Double,
) {

    companion object {

        const val table = "knowledge_level"

        const val columnQuestion = "question"
        const val columnLastAnswerTimestamp = "last_answer_timestamp"
        const val columnStability = "stability"
        const val columnDifficulty = "difficulty"
    }
}
