package hnau.lexplore.dictionaries

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.mapOrAccumulate
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.right
import arrow.core.toNonEmptyListOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifNull
import java.io.File
import java.text.Normalizer

private typealias ParseResult<T> = Either<String, T>

private typealias GenerateResult<T> = Either<NonEmptyList<String>, T>

internal class DictionariesResourceGenerator(
    private val sourceDir: File,
) {

    private val wordCountsFile: File = File(sourceDir, "word_counts.txt")

    private val dictionariesDir: File = File(sourceDir, "dictionaries")

    private val json: Json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    fun generate(): String =
        generateDictionaries().fold(
            ifLeft = { errors -> error(errors.joinToString(separator = "\n")) },
            ifRight = ::render,
        )

    private fun generateDictionaries(): GenerateResult<List<GeneratedDictionary>> = either {
        val counts = readCounts().bind()
        val dictionaries = readDictionaries()
            .bind()
            .map { dictionary -> buildDictionary(dictionary = dictionary, counts = counts) }
        ensureUniqueTranslations(dictionaries).bind()
        dictionaries
    }

    private fun readCounts(): GenerateResult<WordCounts> = either {
        val lines = readLines(wordCountsFile).bind()
        val entries = lines
            .mapOrAccumulate { line -> parseCountLine(line).bind() }
            .bind()
        WordCounts(entries.toMap())
    }

    private fun readDictionaries(): GenerateResult<List<ParsedDictionary>> = either {
        val seenWords = mutableSetOf<String>()
        dictionaryFiles()
            .bind()
            .map { file ->
                val name = file.name.removeSuffix(".$dictionaryExtension")
                val entries = readLines(file)
                    .bind()
                    .mapNotNull { line ->
                        parseDictionaryLine(line).fold(
                            ifLeft = { null },
                            ifRight = { it },
                        )
                    }.filter { entry -> seenWords.add(entry.rawWord.lowercase()) }
                ParsedDictionary(
                    name = name,
                    entries = entries,
                )
            }
    }

    private fun buildDictionary(
        dictionary: ParsedDictionary,
        counts: WordCounts,
    ): GeneratedDictionary =
        GeneratedDictionary(
            name = dictionary.name,
            words = dictionary.entries
                .mapNotNull { entry -> entry.toGeneratedWord(counts) }
                .sortedByDescending(GeneratedWord::weight),
        )

    private fun ensureUniqueTranslations(
        dictionaries: List<GeneratedDictionary>,
    ): GenerateResult<Unit> = either {
        dictionaries
            .flatMap { dictionary ->
                dictionary.words.map { word -> dictionary.name to word }
            }
            .groupBy { (_, word) -> word.translation.lowercase() }
            .filterValues { occurrences -> occurrences.size > 1 }
            .values
            .map { occurrences ->
                val translation = occurrences.first().second.translation
                val words = occurrences.joinToString(separator = ", ") { (dictionaryName, word) ->
                    "'${word.word}' ($dictionaryName)"
                }
                "Translation '$translation' is used for multiple words: $words"
            }
            .toNonEmptyListOrNull()
            .foldNullable(
                ifNull = { },
                ifNotNull = { errors -> raise(errors) },
            )
    }

    private fun parseCountLine(line: String): ParseResult<Pair<String, Int>> {
        val parts = splitLine(line)
        return (parts.size == wordCountsColumns).foldBoolean(
            ifTrue = {
                val word = parts[1].lowercase()
                val count = parts[2].toIntOrNull()
                    ?: return "Expected integer count, got ${parts[2]}".left()
                (word to count).right()
            },
            ifFalse = {
                ("Expected ID|Word|FREQcount|CD|SUBTLEX_WF|Lg10WF|SUBTLEX_CD|Lg10CD|" +
                        "FREQlow|FREQupper|N|OLD20|Length|SUBTLEX_WF_full, got $line").left()
            },
        )
    }

    private fun parseDictionaryLine(line: String): ParseResult<DictionaryEntry> {
        val parts = splitLine(line)
        return (parts.size == 2 || parts.size == 3).foldBoolean(
            ifTrue = {
                val rawWord = parts[0]
                val word = Word.parse(rawWord)
                    ?: return "'$rawWord' has no significant parts".left()
                val explicitWeight = parts
                    .getOrNull(2)
                    .foldNullable(
                        ifNull = { null },
                        ifNotNull = { value ->
                            value.toIntOrNull()
                                ?: return "Expected integer weight, got $value".left()
                        },
                    )
                DictionaryEntry(
                    rawWord = rawWord,
                    word = word,
                    translation = parts[1],
                    explicitWeight = explicitWeight,
                ).right()
            },
            ifFalse = { "Expected <word>|<translation>{|<count>}, got $line".left() },
        )
    }

    private fun dictionaryFiles(): GenerateResult<List<File>> = either {
        val files = dictionariesDir
            .listFiles()
            .orEmpty()
            .sortedBy(File::getName)
        val (dictionaries, invalidFiles) = files.partition { file ->
            file.extension == dictionaryExtension
        }
        invalidFiles
            .map { file -> "Expected file with .$dictionaryExtension extension, got ${file.name}" }
            .toNonEmptyListOrNull()
            .foldNullable(
                ifNull = { dictionaries },
                ifNotNull = { errors -> raise(errors) },
            )
    }

    private fun readLines(file: File): GenerateResult<List<String>> =
        file
            .takeIf(File::exists)
            .foldNullable(
                ifNull = { nonEmptyListOf("File ${file.path} not found").left() },
                ifNotNull = { existingFile ->
                    existingFile
                        .readLines()
                        .map(String::trim)
                        .filterNot { line ->
                            line.isEmpty() || line.startsWith("#") || line.startsWith("//")
                        }.right()
                },
            )

    private fun splitLine(line: String): List<String> =
        line
            .split('|')
            .map(String::trim)
            .filterNot(String::isEmpty)

    private fun render(
        dictionaries: List<GeneratedDictionary>,
    ): String =
        buildJsonArray {
            dictionaries.forEach { dictionary ->
                addJsonObject {
                    put("name", dictionary.name)
                    putJsonArray("words") {
                        dictionary.words.forEach { word ->
                            addJsonObject {
                                put("word", word.word)
                                put("translation", word.translation)
                                put("weight", word.weight)
                            }
                        }
                    }
                }
            }
        }.let { element ->
            json.encodeToString(JsonElement.serializer(), element)
        }

    private data class ParsedDictionary(
        val name: String,
        val entries: List<DictionaryEntry>,
    )

    private data class DictionaryEntry(
        val rawWord: String,
        val word: Word,
        val translation: String,
        val explicitWeight: Int?,
    ) {

        fun toGeneratedWord(
            counts: WordCounts,
        ): GeneratedWord? {
            val weight = explicitWeight
                .foldNullable(
                    ifNull = { word.findCount(counts) },
                    ifNotNull = { it },
                ).ifNull { return null }
            return GeneratedWord(
                word = word.build(),
                translation = translation,
                weight = weight,
            )
        }
    }

    private data class GeneratedDictionary(
        val name: String,
        val words: List<GeneratedWord>,
    )

    private data class GeneratedWord(
        val word: String,
        val translation: String,
        val weight: Int,
    )

    private class WordCounts(
        private val raw: Map<String, Int>,
    ) {

        private val simplified: Map<String, Int> = raw
            .map { (word, count) -> word.withoutNonSpacingMarks() to count }
            .toMap()

        fun find(word: String): Int? {
            val letters = word.filter(Char::isLetter)
            return raw[letters] ?: simplified[letters.withoutNonSpacingMarks()]
        }
    }

    private class Word(
        val parts: NonEmptyList<String>,
    ) {

        fun build(): String =
            parts.joinToString(separator = " ")

        fun findCount(
            counts: WordCounts,
        ): Int? =
            parts
                .map(String::lowercase)
                .mapNotNull(counts::find)
                .minOrNull()

        companion object {

            fun parse(source: String): Word? =
                source
                    .split(' ')
                    .map(String::trim)
                    .filterNot(String::isEmpty)
                    .filterNot { part -> part.startsWith('[') && part.endsWith(']') }
                    .toNonEmptyListOrNull()
                    .foldNullable(
                        ifNull = { null },
                        ifNotNull = { parts -> Word(parts = parts) },
                    )
        }
    }
}

private const val dictionaryExtension = "txt"

private const val wordCountsColumns = 14

private fun String.withoutNonSpacingMarks(): String =
    Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .filter { char -> Character.getType(char) != Character.NON_SPACING_MARK.toInt() }
