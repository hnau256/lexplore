package hnau.lexplore.dictionaries

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.mapOrAccumulate
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import kotlinx.serialization.Serializable
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

@Serializable
private data class WordsFile(
    val words: List<WordEntryJson>,
)

@Serializable
private data class WordEntryJson(
    val word: String,
    val translation: String,
    val pos: String,
    val level: Level,
    val theme: String,
    val subtheme: String? = null,
    val lemma: String? = null,
    val weight: Int? = null,
    val status: Status = Status.active,
    val notes: String? = null,
)

private sealed interface PartOfSpeech {

    data class Noun(
        val form: Form = Form.Base,
    ) : PartOfSpeech {

        enum class Form {
            Base,
            Declension,
        }
    }

    data class Verb(
        val form: Form = Form.Base,
    ) : PartOfSpeech {

        enum class Form {
            Base,
            Aorist,
            Conjugation,
            Irregular,
        }
    }

    data object Adjective : PartOfSpeech

    data object Adverb : PartOfSpeech

    data object Pronoun : PartOfSpeech

    data object Numeral : PartOfSpeech

    data object Phrase : PartOfSpeech

    data object Particle : PartOfSpeech

    data object Preposition : PartOfSpeech

    data object Conjunction : PartOfSpeech

    data object Interjection : PartOfSpeech
}

private val partOfSpeechRegex = Regex("""^([a-z]+)(?:\s*\[([a-z]+)])?$""")

private fun parsePartOfSpeech(value: String): ParseResult<PartOfSpeech> = either {
    val normalized = value.trim()
    if (normalized.isEmpty()) {
        raise("part of speech is empty")
    }
    val match = partOfSpeechRegex.matchEntire(normalized)
        ?: raise("invalid part of speech '$value'")
    val base = match.groupValues[1]
    val form = match.groupValues[2].takeIf(String::isNotEmpty)
    when (base) {
        "noun" -> PartOfSpeech.Noun(
            form = when (form) {
                null -> PartOfSpeech.Noun.Form.Base
                "declension" -> PartOfSpeech.Noun.Form.Declension
                else -> raise("unknown noun form '$form'")
            },
        )

        "verb" -> PartOfSpeech.Verb(
            form = when (form) {
                null -> PartOfSpeech.Verb.Form.Base
                "aorist" -> PartOfSpeech.Verb.Form.Aorist
                "conjugation" -> PartOfSpeech.Verb.Form.Conjugation
                "irregular" -> PartOfSpeech.Verb.Form.Irregular
                else -> raise("unknown verb form '$form'")
            },
        )

        else -> {
            if (form != null) {
                raise("part of speech '$base' does not support form '$form'")
            }
            when (base) {
                "adjective" -> PartOfSpeech.Adjective
                "adverb" -> PartOfSpeech.Adverb
                "pronoun" -> PartOfSpeech.Pronoun
                "numeral" -> PartOfSpeech.Numeral
                "phrase" -> PartOfSpeech.Phrase
                "particle" -> PartOfSpeech.Particle
                "preposition" -> PartOfSpeech.Preposition
                "conjunction" -> PartOfSpeech.Conjunction
                "interjection" -> PartOfSpeech.Interjection
                else -> raise("unknown part of speech '$base'")
            }
        }
    }
}

@Serializable
private enum class Level {
    A1,
    A2,
    B1,
    B2,
    C1,
    C2,
}

@Serializable
private enum class Status {
    active,
    hidden,
}

internal class DictionariesResourceGenerator(
    private val sourceDir: File,
) {

    private val wordCountsFile: File = File(sourceDir, "word_counts.txt")

    private val wordsFile: File = File(sourceDir, "words.json")

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
        val rows = readWordsJson().bind()
        val errors = mutableListOf<String>()
        val dictionaries = rows
            .filter { row -> row.status == Status.active }
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

    private fun readWordsJson(): GenerateResult<List<WordRow>> = either {
        if (!wordsFile.exists()) {
            raise(nonEmptyListOf("File ${wordsFile.path} not found"))
        }
        val table = try {
            json.decodeFromString(
                deserializer = WordsFile.serializer(),
                string = wordsFile.readText(),
            )
        } catch (e: Exception) {
            raise(nonEmptyListOf("${wordsFile.name}: ${e.message ?: e::class.simpleName}"))
        }
        val errors = mutableListOf<String>()
        val rows = table.words
            .mapIndexedNotNull { index, entry ->
                entry.toWordRow(index = index).fold(
                    ifLeft = { error -> errors += error; null },
                    ifRight = { it },
                )
            }
        errors.toNonEmptyListOrNull()?.let { raise(it) }
        validateRows(rows).bind()
        rows
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

    private fun validateRows(
        rows: List<WordRow>,
    ): GenerateResult<Unit> = either {
        val errors = mutableListOf<String>()
        val seenWords = mutableMapOf<String, String>()
        val seenTranslations = mutableMapOf<String, String>()
        val allWords = rows.map(WordRow::rawWord).toSet()
        rows.forEach { row ->
            val wordKey = row.rawWord.lowercase()
            val previousWord = seenWords.putIfAbsent(wordKey, row.source)
            if (previousWord != null) {
                errors += "${row.source}: duplicate word '${row.rawWord}' (first at $previousWord)"
            }
            val translationKey = row.translation.lowercase()
            val previousTranslation = seenTranslations.putIfAbsent(translationKey, row.source)
            if (previousTranslation != null) {
                errors += "${row.source}: duplicate translation '${row.translation}' " +
                        "(first at $previousTranslation)"
            }
            if (row.lemma.isNotEmpty() && row.lemma !in allWords) {
                errors += "${row.source}: lemma '${row.lemma}' not found"
            }
        }
        errors.toNonEmptyListOrNull()?.let { raise(it) }
    }

    private fun WordEntryJson.toWordRow(
        index: Int,
    ): ParseResult<WordRow> = either {
        val location = "words.json [${index + 1}] '$word'"
        if (word.isEmpty()) {
            raise("$location: field 'word' is empty")
        }
        if (translation.isEmpty()) {
            raise("$location: field 'translation' is empty")
        }
        if (theme.isEmpty()) {
            raise("$location: field 'theme' is empty")
        }
        if (weight != null && weight < 0) {
            raise("$location: weight must be non-negative")
        }
        WordRow(
            source = location,
            rawWord = word,
            word = parseWord(location = location, rawWord = word).bind(),
            translation = translation,
            pos = parsePartOfSpeech(pos).bind(),
            level = level,
            theme = theme,
            subtheme = subtheme.orEmpty(),
            lemma = lemma.orEmpty(),
            explicitWeight = weight,
            status = status,
        )
    }

    private fun parseCountLine(line: SourceLine): ParseResult<WordCountEntry> = either {
        val parts = splitLine(line.text)
        if (parts.size != wordCountsColumns || parts.any(String::isEmpty)) {
            raise(
                "${line.location}: Expected ID|Word|FREQcount|CD|SUBTLEX_WF|Lg10WF|" +
                        "SUBTLEX_CD|Lg10CD|FREQlow|FREQupper|N|OLD20|Length|SUBTLEX_WF_full, got ${line.text}"
            )
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
        location: String,
        rawWord: String,
    ): ParseResult<Word> = either {
        val tokens = rawWord.split(' ')
        if (tokens.any(String::isEmpty)) {
            raise("$location: Word '$rawWord' contains repeated spaces")
        }
        val significantParts = mutableListOf<String>()
        tokens.forEachIndexed { index, token ->
            val isLast = index == tokens.lastIndex
            val isAdditional = token.startsWith('[') || token.endsWith(']')
            if (isAdditional) {
                if (!token.startsWith('[') || !token.endsWith(']')) {
                    raise("$location: Word '$rawWord' has an invalid bracket part '$token'")
                }
                val inner = token.substring(1, token.length - 1)
                if (inner.isEmpty() || !inner.isGreekWord()) {
                    raise("$location: Word '$rawWord' has an invalid bracket part '$token'")
                }
            } else {
                if (token.endsWith(';') && !isLast) {
                    raise("$location: Word '$rawWord' has ';' not at the end")
                }
                val word = token.removeSuffix(";")
                if (!word.isGreekWord()) {
                    raise("$location: Word '$rawWord' contains non-Greek characters in '$token'")
                }
                significantParts += word
            }
        }
        val parts = significantParts.toNonEmptyListOrNull()
            ?: raise("$location: Word '$rawWord' has no significant parts")
        Word(parts = parts)
    }

    private fun readLines(file: File): GenerateResult<List<SourceLine>> = either {
        val existingFile = file
            .takeIf(File::exists)
            ?: raise(nonEmptyListOf("File ${file.path} not found"))
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
            }
    }

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

    private data class WordCountEntry(
        val source: SourceLine,
        val word: String,
        val count: Int,
    )

    private data class WordRow(
        val source: String,
        val rawWord: String,
        val word: Word,
        val translation: String,
        val pos: PartOfSpeech,
        val level: Level,
        val theme: String,
        val subtheme: String,
        val lemma: String,
        val explicitWeight: Int?,
        val status: Status,
    ) {

        fun toGeneratedWord(
            counts: WordCounts,
        ): ParseResult<GeneratedWord> = either {
            val weight = explicitWeight.foldNullable(
                ifNull = { word.findCount(counts).bind() },
                ifNotNull = { it },
            )
            GeneratedWord(
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
        ): ParseResult<Int> = either {
            val countsByPart = parts.map { part ->
                val word = part.lowercase()
                word to counts.find(word)
            }
            val missing = countsByPart
                .filter { (_, count) -> count == null }
                .map { (word, _) -> word }
            if (missing.isNotEmpty()) {
                raise("Unknown count of word(s): ${missing.joinToString(separator = ", ")}")
            }
            countsByPart
                .mapNotNull { (_, count) -> count }
                .minOrNull()
                ?: raise("Unknown count of word(s): ${parts.joinToString(separator = ", ")}")
        }
    }
}

private const val wordCountsColumns = 14

private fun String.withoutNonSpacingMarks(): String =
    Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .filter { char -> Character.getType(char) != Character.NON_SPACING_MARK.toInt() }
