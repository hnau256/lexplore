package hnau.lexplore.exercise.dto.dictionary.provider

import android.content.Context
import arrow.core.Either
import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.common.kotlin.removePrefixOrNull
import hnau.lexplore.common.kotlin.removeSuffixOrNull
import hnau.lexplore.common.kotlin.tokenize.Tokenizer
import hnau.lexplore.common.kotlin.tokenize.tokenize
import hnau.lexplore.exercise.dto.Translation
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.Dictionary
import hnau.lexplore.exercise.dto.dictionary.DictionaryName

object VerbsDictionariesProvider : DictionariesProvider {

    override suspend fun loadList(
        context: Context,
    ): Dictionaries {
        val name = DictionaryName("Спряжение глаголов")
        val dictionary = Dictionary.create(
            words = loadWords(
                context = context,
            )
        )
        return Dictionaries(
            mapOf(name to dictionary),
        )
    }

    private suspend fun loadWords(
        context: Context,
    ): List<Word> = DictionariesProviderUtils
        .readBlocksWithIndexRanges(
            stream = context.assets.open("verbs.txt")
        )
        .flatMap { block ->
            val newValues = block
                .values
                .asSequence()
                .map { line ->
                    when (val endings = line.tryParseToVerbsEndings()) {
                        null -> {
                            val (verb, translation) = line
                                .split('|')
                                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                                .takeIf { it.size == 2 }
                                .ifNull { throw IllegalArgumentException("Expected '<greekVerb>|<translation>', got $line") }
                            Either.Left(WordToLearn(verb) to Translation(translation))
                        }

                        else -> Either.Right(endings)
                    }
                }
                .tokenize { lineOrVerbsEndings ->
                    when (lineOrVerbsEndings) {
                        is Either.Left -> throw IllegalStateException("Expected verbs endings, got ${lineOrVerbsEndings.value}")
                        is Either.Right -> {
                            VerbsTokenizer(
                                endings = lineOrVerbsEndings.value,
                            )
                        }
                    }
                }
                .flatMap { it }
                .toList()
                .sortedBy { (verb) -> verb.word.hashCode() }
            BlockWithIndexRange(
                minIndex = block.minIndex,
                maxIndex = block.maxIndex,
                values = newValues,
            ).build()
        }
        .map { (index, verbWithTranslation) ->
            Word(
                index = index,
                toLearn = verbWithTranslation.first,
                translation = verbWithTranslation.second,
            )
        }
        .toList()


    private class VerbsTokenizer(
        private val endings: Variants<String>,
        private val collectedVerbs: List<Pair<WordToLearn, Translation>> = emptyList(),
    ) : Tokenizer<Either<Pair<WordToLearn, Translation>, Variants<String>>, List<Pair<WordToLearn, Translation>>> {

        override fun collect(): List<Pair<WordToLearn, Translation>> =
            collectedVerbs.flatMap { verbWithTranslation ->
                val verbPrefix = verbWithTranslation.first.word
                val translation = verbWithTranslation.second.translation
                Variant.entries.map { variant ->
                    val verb =
                        greekPronouns[variant] + " " + verbPrefix + endings[variant]
                    val translationWithPronouns =
                        "(" + russianPronouns[variant] + ") " + translation
                    WordToLearn(verb) to Translation(translationWithPronouns)
                }
            }

        override fun tryConsume(
            nextItem: Either<Pair<WordToLearn, Translation>, Variants<String>>,
        ): Tokenizer<Either<Pair<WordToLearn, Translation>, Variants<String>>, List<Pair<WordToLearn, Translation>>>? =
            when (nextItem) {
                is Either.Left -> VerbsTokenizer(
                    endings = endings,
                    collectedVerbs = collectedVerbs + nextItem.value
                )

                is Either.Right -> null
            }


    }

    private enum class Variant { S1, S2, S3, P1, P2, P3 }

    private data class Variants<T>(
        val s1: T,
        val s2: T,
        val s3: T,
        val p1: T,
        val p2: T,
        val p3: T,
    ) {

        operator fun get(
            variant: Variant,
        ): T = when (variant) {
            Variant.S1 -> s1
            Variant.S2 -> s2
            Variant.S3 -> s3
            Variant.P1 -> p1
            Variant.P2 -> p2
            Variant.P3 -> p3
        }
    }

    private fun String.tryParseToVerbsEndings(): Variants<String>? = this
        .removePrefixOrNull("{")
        ?.removeSuffixOrNull("}")
        ?.let { titleWithEndings ->
            val parts = titleWithEndings
                .split(',')
                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                .takeIf { it.size == 6 }
                .ifNull { throw IllegalArgumentException("Expected '[<name>,<1s>,<2s>,<3s>,<1p>,<2p>,<3p>]', got $this") }
            Variants(
                s1 = parts[0],
                s2 = parts[1],
                s3 = parts[2],
                p1 = parts[3],
                p2 = parts[4],
                p3 = parts[5],
            )
        }

    private val russianPronouns: Variants<String> = Variants(
        s1 = "я",
        s2 = "ты",
        s3 = "он",
        p1 = "мы",
        p2 = "вы",
        p3 = "они",
    )

    private val greekPronouns: Variants<String> = Variants(
        s1 = "εγώ",
        s2 = "εσύ",
        s3 = "αυτός",
        p1 = "εμείς",
        p2 = "εσείς",
        p3 = "αυτοί",
    )
}