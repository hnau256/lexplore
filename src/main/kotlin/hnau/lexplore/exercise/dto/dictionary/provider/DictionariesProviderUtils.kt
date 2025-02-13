package hnau.lexplore.exercise.dto.dictionary.provider

import androidx.compose.ui.util.lerp
import arrow.core.Either
import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.common.kotlin.removePrefixOrNull
import hnau.lexplore.common.kotlin.removeSuffixOrNull
import hnau.lexplore.common.kotlin.tokenize.Tokenizer
import hnau.lexplore.common.kotlin.tokenize.tokenize
import hnau.lexplore.exercise.dto.Translation
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.WordToLearn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.Reader

object DictionariesProviderUtils {

    private suspend fun readLines(
        stream: InputStream,
    ): Sequence<String> = withContext(Dispatchers.IO) {
        stream.use { inputStream ->
            inputStream
                .reader()
                .use(Reader::readLines)
        }
    }.let { lines ->
        withContext(Dispatchers.Default) {
            lines
                .asSequence()
                .map(String::trim)
                .filterNot { line ->
                    line.isEmpty() ||
                            line.startsWith("//") ||
                            line.startsWith("#")
                }
        }
    }

    suspend fun readBlocksWithIndexRanges(
        stream: InputStream,
    ): Sequence<BlockWithIndexRange<String>> = readLines(
        stream = stream,
    )
        .map { line ->
            when (val range = line.tryParseToIndexRange()) {
                null -> Either.Left(line)
                else -> Either.Right(range)
            }
        }
        .tokenize { lineOrRange ->
            when (lineOrRange) {
                is Either.Left -> throw IllegalStateException("Expected indexes, got ${lineOrRange.value}")
                is Either.Right -> {
                    val (minIndex, maxIndex) = lineOrRange.value
                    IndexRangeTokenizer(
                        minIndex = minIndex,
                        maxIndex = maxIndex,
                    )
                }
            }
        }

    private class IndexRangeTokenizer(
        private val minIndex: Float,
        private val maxIndex: Float,
        private val collectedLines: List<String> = emptyList(),
    ) : Tokenizer<Either<String, Pair<Float, Float>>, BlockWithIndexRange<String>> {

        override fun collect(): BlockWithIndexRange<String> = BlockWithIndexRange(
            maxIndex = maxIndex,
            minIndex = minIndex,
            values = collectedLines,
        )

        override fun tryConsume(
            nextItem: Either<String, Pair<Float, Float>>,
        ): Tokenizer<Either<String, Pair<Float, Float>>, BlockWithIndexRange<String>>? = nextItem
            .leftOrNull()
            ?.let { line ->
                IndexRangeTokenizer(
                    minIndex = minIndex,
                    maxIndex = maxIndex,
                    collectedLines = collectedLines + line
                )
            }
    }

    private fun String.tryParseToIndexRange(): Pair<Float, Float>? = this
        .removePrefixOrNull("[")
        ?.removeSuffixOrNull("]")
        ?.let { minWithMax ->
            val (min, max) = minWithMax
                .split(',')
                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                .takeIf { it.size == 2 }
                ?.mapNotNull { it.toFloatOrNull() }
                ?.takeIf { it.size == 2 }
                .ifNull { throw IllegalArgumentException("Expected '[<min>,<max>]', got $this") }
            min to max
        }
}