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
import org.hnau.commons.kotlin.foldNullable
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
        val parsedDictionaries = readDictionaries().bind()
        val errors = mutableListOf<String>()
        val dictionaries = parsedDictionaries.map { dictionary ->
            GeneratedDictionary(
                name = dictionary.name,
                words = dictionary.entries
                    .mapNotNull { entry ->
                        entry.toGeneratedWord(counts).fold(
                            ifLeft = { error -> errors += error; null },
                            ifRight = { it },
                        )
                    }
                    .sortedByDescending(GeneratedWord::weight),
            )
        }
        errors.toNonEmptyListOrNull()?.let { raise(it) }
        ensureUniqueTranslations(dictionaries).bind()
        dictionaries
    }

    private fun readCounts(): GenerateResult<WordCounts> = either {
        val lines = readLines(wordCountsFile).bind()
        val entries = lines
            .mapOrAccumulate { line -> parseCountLine(line).bind() }
            .bind()
        val duplicates = entries
            .groupBy(WordCountEntry::word)
            .filterValues { occurrences -> occurrences.size > 1 }
            .values
            .map { occurrences ->
                val location = occurrences.joinToString(separator = ", ") { it.source.location }
                "Duplicate word '${occurrences.first().word}' at $location"
            }
        duplicates.toNonEmptyListOrNull()?.let { raise(it) }
        WordCounts(entries.associate { entry -> entry.word to entry.count })
    }

    private fun readDictionaries(): GenerateResult<List<ParsedDictionary>> = either {
        val errors = mutableListOf<String>()
        val seenWords = mutableMapOf<String, SourceLine>()
        val dictionaries = dictionaryFiles()
            .bind()
            .mapNotNull { file ->
                val name = file.name.removeSuffix(".$dictionaryExtension")
                if (name.isEmpty()) {
                    errors += "${file.name}: dictionary name is empty"
                    return@mapNotNull null
                }
                val entries = readLines(file)
                    .bind()
                    .mapNotNull { line ->
                        parseDictionaryLine(line).fold(
                            ifLeft = { error -> errors += error; null },
                            ifRight = { entry ->
                                val key = entry.rawWord.lowercase()
                                val previous = seenWords.putIfAbsent(key, line)
                                if (previous != null) {
                                    errors += "${line.location}: duplicate word '${entry.rawWord}' " +
                                        "(first at ${previous.location})"
                                    null
                                } else {
                                    entry
                                }
                            },
                        )
                    }
                ParsedDictionary(
                    name = name,
                    entries = entries,
                )
            }
        errors.toNonEmptyListOrNull()?.let { raise(it) }
        dictionaries
    }

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

    private fun parseCountLine(line: SourceLine): ParseResult<WordCountEntry> = either {
        val parts = splitLine(line.text)
        if (parts.size != wordCountsColumns || parts.any(String::isEmpty)) {
            raise("${line.location}: Expected ID|Word|FREQcount|CD|SUBTLEX_WF|Lg10WF|" +
                "SUBTLEX_CD|Lg10CD|FREQlow|FREQupper|N|OLD20|Length|SUBTLEX_WF_full, got ${line.text}")
        }
        if (!parts[1].isGreekWord()) {
            raise("${line.location}: Expected Greek word, got '${parts[1]}'")
        }
        val count = parts[2].toIntOrNull()?.takeIf { it >= 0 }
            ?: raise("${line.location}: Expected non-negative integer count, got '${parts[2]}'")
        WordCountEntry(
            source = line,
            word = parts[1].lowercase(),
            count = count,
        )
    }

    private fun parseDictionaryLine(line: SourceLine): ParseResult<DictionaryEntry> = either {
        val parts = splitLine(line.text)
        if ((parts.size != 2 && parts.size != 3) || parts.any(String::isEmpty)) {
            raise("${line.location}: Expected <word>|<translation>{|<count>}, got ${line.text}")
        }
        val rawWord = parts[0]
        val word = parseWord(line = line, rawWord = rawWord).bind()
        val explicitWeight = parts
            .getOrNull(2)
            ?.let { value ->
                value.toIntOrNull()?.takeIf { it >= 0 }
                    ?: raise("${line.location}: Expected non-negative integer weight, got '$value'")
            }
        DictionaryEntry(
            source = line,
            rawWord = rawWord,
            word = word,
            translation = parts[1],
            explicitWeight = explicitWeight,
        )
    }

    private fun parseWord(
        line: SourceLine,
        rawWord: String,
    ): ParseResult<Word> = either {
        val tokens = rawWord.split(' ')
        if (tokens.any(String::isEmpty)) {
            raise("${line.location}: Word '$rawWord' contains repeated spaces")
        }
        val significantParts = mutableListOf<String>()
        tokens.forEachIndexed { index, token ->
            val isLast = index == tokens.lastIndex
            val isAdditional = token.startsWith('[') || token.endsWith(']')
            if (isAdditional) {
                if (!token.startsWith('[') || !token.endsWith(']')) {
                    raise("${line.location}: Word '$rawWord' has an invalid bracket part '$token'")
                }
                val inner = token.substring(1, token.length - 1)
                if (inner.isEmpty() || !inner.isGreekWord()) {
                    raise("${line.location}: Word '$rawWord' has an invalid bracket part '$token'")
                }
            } else {
                if (token.endsWith(';') && !isLast) {
                    raise("${line.location}: Word '$rawWord' has ';' not at the end")
                }
                val word = token.removeSuffix(";")
                if (!word.isGreekWord()) {
                    raise("${line.location}: Word '$rawWord' contains non-Greek characters in '$token'")
                }
                significantParts += word
            }
        }
        val parts = significantParts.toNonEmptyListOrNull()
            ?: raise("${line.location}: Word '$rawWord' has no significant parts")
        Word(parts = parts)
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

    private fun readLines(file: File): GenerateResult<List<SourceLine>> =
        file
            .takeIf(File::exists)
            .foldNullable(
                ifNull = { nonEmptyListOf("File ${file.path} not found").left() },
                ifNotNull = { existingFile ->
                    existingFile
                        .readLines()
                        .mapIndexedNotNull { index, line ->
                            line
                                .trim()
                                .takeIf { trimmed ->
                                    trimmed.isNotEmpty() &&
                                        !trimmed.startsWith("#") &&
                                        !trimmed.startsWith("//")
                                }
                                ?.let { trimmed ->
                                    SourceLine(
                                        file = file,
                                        number = index + 1,
                                        text = trimmed,
                                    )
                                }
                        }.right()
                },
            )

    private fun splitLine(line: String): List<String> =
        line
            .split('|')
            .map(String::trim)

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

    private fun String.isGreekWord(): Boolean =
        isNotEmpty() && all { char ->
            Character.UnicodeScript.of(char.code) == Character.UnicodeScript.GREEK
        }

    private data class SourceLine(
        val file: File,
        val number: Int,
        val text: String,
    ) {

        val location: String
            get() = "${file.name}:$number"
    }

    private data class ParsedDictionary(
        val name: String,
        val entries: List<DictionaryEntry>,
    )

    private data class WordCountEntry(
        val source: SourceLine,
        val word: String,
        val count: Int,
    )

    private data class DictionaryEntry(
        val source: SourceLine,
        val rawWord: String,
        val word: Word,
        val translation: String,
        val explicitWeight: Int?,
    ) {

        fun toGeneratedWord(
            counts: WordCounts,
        ): ParseResult<GeneratedWord> {
            val weight = explicitWeight.foldNullable(
                ifNull = {
                    word
                        .findCount(counts)
                        .fold(
                            ifLeft = { error -> return "${source.location}: $error".left() },
                            ifRight = { it },
                        )
                },
                ifNotNull = { it },
            )
            return GeneratedWord(
                word = word.build(),
                translation = translation,
                weight = weight,
            ).right()
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
        ): ParseResult<Int> {
            val countsByPart = parts.map { part ->
                val word = part.lowercase()
                word to counts.find(word)
            }
            val missing = countsByPart
                .filter { (_, count) -> count == null }
                .map { (word, _) -> word }
            if (missing.isNotEmpty()) {
                return "Unknown count of word(s): ${missing.joinToString(separator = ", ")}".left()
            }
            val count = countsByPart
                .mapNotNull { (_, count) -> count }
                .minOrNull()
                ?: return "Unknown count of word(s): ${parts.joinToString(separator = ", ")}".left()
            return count.right()
        }
    }
}

private const val dictionaryExtension = "txt"

private const val wordCountsColumns = 14

private fun String.withoutNonSpacingMarks(): String =
    Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .filter { char -> Character.getType(char) != Character.NON_SPACING_MARK.toInt() }
