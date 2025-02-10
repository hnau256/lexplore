package hnau.lexplore.data.knowledge.db

import androidx.room.TypeConverter
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.WordToLearn
import kotlinx.datetime.Instant

class KnowledgeDatabaseTypeConverters {

    @TypeConverter
    fun serializeQuestionKey(
        from: WordToLearn,
    ): String = from.word

    @TypeConverter
    fun deserializeQuestionKey(
        from: String,
    ): WordToLearn = WordToLearn(
        word = from,
    )

    @TypeConverter
    fun serializeKnowledgeLevel(
        from: ForgettingFactor,
    ): Float = from.factor

    @TypeConverter
    fun deserializeKnowledgeLevel(
        from: Float,
    ): ForgettingFactor = ForgettingFactor(
        factor = from,
    )

    @TypeConverter
    fun serializeInstant(
        from: Instant,
    ): Long = from.epochSeconds

    @TypeConverter
    fun deserializeInstant(
        from: Long,
    ): Instant = Instant.fromEpochSeconds(
        epochSeconds = from,
    )
}