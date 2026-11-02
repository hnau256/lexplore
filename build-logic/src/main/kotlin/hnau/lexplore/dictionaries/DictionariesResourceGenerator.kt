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

private val wordsTableColumns = listOf(
    "word",
    "translation",
    "pos",
    "level",
    "theme",
    "subtheme",
    "form_type",
    "lemma",
    "weight",
    "status",
    "notes",
)

private val allowedPos = setOf(
    "noun",
    "verb",
    "adjective",
    "adverb",
    "pronoun",
    "numeral",
    "phrase",
    "particle",
    "preposition",
    "conjunction",
    "interjection",
)

private val allowedLevels = setOf(
    "A1",
    "A2",
    "B1",
    "B2",
    "C1",
)

internal class DictionariesResourceGenerator(
    private val sourceDir: File,
) {

    private val wordCountsFile: File = File(sourceDir, "word_counts.txt")

    private val wordsFile: File = File(sourceDir, "words.tsv")

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
        val rows = readWordsTable().bind()
        val errors = mutableListOf<String>()
        val dictionaries = rows
            .filter { row -> row.status == EntryStatus.active }
            .groupBy(WordRow::theme)
            .entries
            .sortedBy { (theme, _) -> theme }
            .map { (theme, themeRows) ->
                GeneratedDictionary(
                    name = theme,
                    words = themeRows
                        .mapNotNull { row ->
                            row.toGeneratedWord(counts).fold(
                                ifLeft = { error -> errors += error; null },
                                ifRight = { it },
                            )
                        }
                        .sortedByDescending(GeneratedWord::weight),
                )
            }
        errors.toNonEmptyListOrNull()?.let { raise(it) }
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

    private fun readWordsTable(): GenerateResult<List<WordRow>> = either {
        val lines = readLines(wordsFile).bind()
        val headerLine = lines.firstOrNull()
            ?: raise(nonEmptyListOf("File ${wordsFile.path} is empty"))
        val header = splitTsv(headerLine.text.removePrefix("\uFEFF"))
            .map(String::trim)
        if (header.size != wordsTableColumns.size || header.toSet() != wordsTableColumns.toSet()) {
            raise(
                nonEmptyListOf(
                    "${headerLine.location}: expected header ${wordsTableColumns.joinToString(separator = "\t")}, " +
                        "got ${headerLine.text}"
                )
            )
        }
        val errors = mutableListOf<String>()
        val rows = lines
            .drop(1)
            .mapNotNull { line ->
                parseWordRow(line = line, header = header).fold(
                    ifLeft = { error -> errors += error; null },
                    ifRight = { it },
                )
            }
        errors.toNonEmptyListOrNull()?.let { raise(it) }
        validateRows(rows).bind()
        rows
    }

    private fun parseWordRow(
        line: SourceLine,
        header: List<String>,
    ): ParseResult<WordRow> = either {
        val fields = splitTsv(line.text).map(String::trim)
        if (fields.size != header.size) {
            raise("${line.location}: expected ${header.size} columns, got ${fields.size}")
        }
        val values = header
            .indices
            .associate { index -> header[index] to fields[index] }
        val wordString = values.getValue("word")
        if (wordString.isEmpty()) {
            raise("${line.location}: field 'word' is empty")
        }
        val translation = values.getValue("translation")
        if (translation.isEmpty()) {
            raise("${line.location}: field 'translation' is empty")
        }
        val pos = values.getValue("pos")
        if (pos !in allowedPos) {
            raise("${line.location}: unknown part of speech '$pos'")
        }
        val level = values.getValue("level")
        if (level !in allowedLevels) {
            raise("${line.location}: unknown level '$level'")
        }
        val status = when (val value = values.getValue("status")) {
            "active" -> EntryStatus.active
            "hidden" -> EntryStatus.hidden
            else -> raise("${line.location}: unknown status '$value'")
        }
        val theme = values.getValue("theme")
        if (theme.isEmpty()) {
            raise("${line.location}: field 'theme' is empty")
        }
        val explicitWeight = values
            .getValue("weight")
            .takeIf(String::isNotEmpty)
            ?.let { value ->
                value.toIntOrNull()?.takeIf { it >= 0 }
                    ?: raise("${line.location}: expected non-negative integer weight, got '$value'")
            }
        val word = parseWord(line = line, rawWord = wordString).bind()
        WordRow(
            source = line,
            rawWord = wordString,
            word = word,
            translation = translation,
            pos = pos,
            level = level,
            theme = theme,
            subtheme = values.getValue("subtheme"),
            formType = values.getValue("form_type"),
            lemma = values.getValue("lemma"),
            explicitWeight = explicitWeight,
            status = status,
        )
    }

    private fun validateRows(
        rows: List<WordRow>,
    ): GenerateResult<Unit> = either {
        val errors = mutableListOf<String>()
        val seenWords = mutableMapOf<String, SourceLine>()
        val seenTranslations = mutableMapOf<String, SourceLine>()
        val allWords = rows.map(WordRow::rawWord).toSet()
        rows.forEach { row ->
            val wordKey = row.rawWord.lowercase()
            val previousWord = seenWords.putIfAbsent(wordKey, row.source)
            if (previousWord != null) {
                errors += "${row.source.location}: duplicate word '${row.rawWord}' " +
                    "(first at ${previousWord.location})"
            }
            val translationKey = row.translation.lowercase()
            val previousTranslation = seenTranslations.putIfAbsent(translationKey, row.source)
            if (previousTranslation != null) {
                errors += "${row.source.location}: duplicate translation '${row.translation}' " +
                    "(first at ${previousTranslation.location})"
            }
            if (row.lemma.isNotEmpty() && row.lemma !in allWords) {
                errors += "${row.source.location}: lemma '${row.lemma}' not found"
            }
        }
        errors.toNonEmptyListOrNull()?.let { raise(it) }
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

    private fun readLines(file: File): GenerateResult<List<SourceLine>> =
        file
            .takeIf(File::exists)
            .foldNullable(
                ifNull = { nonEmptyListOf("File ${file.path} not found").left() },
                ifNotNull = { existingFile ->
                    existingFile
                        .readLines()
                        .mapIndexedNotNull { index, rawLine ->
                            val line = rawLine.removeSuffix("\r")
                            val trimmed = line.trim()
                            line
                                .takeIf {
                                    trimmed.isNotEmpty() &&
                                        !trimmed.startsWith("#") &&
                                        !trimmed.startsWith("//")
                                }
                                ?.let {
                                    SourceLine(
                                        file = file,
                                        number = index + 1,
                                        text = line,
                                    )
                                }
                        }.right()
                },
            )

    private fun splitLine(line: String): List<String> =
        line
            .split('|')
            .map(String::trim)

    private fun splitTsv(line: String): List<String> {
        val result = mutableListOf<String>()
        var start = 0
        line.forEachIndexed { index, char ->
            if (char == '\t') {
                result += line.substring(start, index)
                start = index + 1
            }
        }
        result += line.substring(start)
        return result
    }

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

    private data class WordCountEntry(
        val source: SourceLine,
        val word: String,
        val count: Int,
    )

    private enum class EntryStatus {
        active,
        hidden,
    }

    private data class WordRow(
        val source: SourceLine,
        val rawWord: String,
        val word: Word,
        val translation: String,
        val pos: String,
        val level: String,
        val theme: String,
        val subtheme: String,
        val formType: String,
        val lemma: String,
        val explicitWeight: Int?,
        val status: EntryStatus,
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

private const val wordCountsColumns = 14

private fun String.withoutNonSpacingMarks(): String =
    Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .filter { char -> Character.getType(char) != Character.NON_SPACING_MARK.toInt() }
