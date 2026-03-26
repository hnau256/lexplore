import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import java.io.File
import java.text.Normalizer
import kotlin.text.lowercase

tasks.register<BuildDictionariesResourceTask>("buildDictionariesResource") {
    sourceDir = file("data")
    outputDir = file("src/main/res/raw")
}

open class BuildDictionariesResourceTask : DefaultTask() {
    @InputDirectory
    lateinit var sourceDir: File

    @OutputDirectory
    lateinit var outputDir: File

    @TaskAction
    fun generate() {
        val errors = mutableListOf<String>()

        val counts =
            Counts(
                raw =
                    File(sourceDir, "word_counts.txt")
                        .readLines(
                            onError = { line, msg -> errors.add(msg) },
                            buildEntry = { line, values ->
                                if (values.size != 14) {
                                    error(
                                        "Expected ID|Word|FREQcount|CD|SUBTLEX_WF|Lg10WF|SUBTLEX_CD|Lg10CD|FREQlow|FREQupper|N|OLD20|Length|SUBTLEX_WF_full, got $line",
                                    )
                                }
                                val word = values[1].lowercase()
                                val count = values[2].toInt()
                                word to count
                            },
                        ).associate { it },
                errors = errors,
            )

        val dictionariesDir = File(sourceDir, "dictionaries")
        val collectedWords: MutableSet<String> = mutableSetOf()
        val dictionaries =
            dictionariesDir
                .list()
                .orEmpty()
                .sorted()
                .mapNotNull { dictionaryFileName ->
                    val dictionaryName = dictionaryFileName.removeSuffix(dictionaryExtensionSuffix)
                    if (dictionaryName == dictionaryFileName) {
                        errors.add("Expected file with $dictionaryExtensionSuffix extension, got $dictionaryFileName")
                        return@mapNotNull null
                    }
                    val words =
                        File(dictionariesDir, dictionaryFileName)
                            .readLines(
                                onError = { line, msg -> errors.add("[$dictionaryFileName] $msg") },
                                buildEntry = { line, parts ->
                                    if (parts.size != 2 && parts.size != 3) {
                                        error("Expected <word>|<translation>{|<count>}, got $line")
                                    }
                                    val wordString = parts[0].trim()

                                    val collectedWord = wordString.lowercase()
                                    if (collectedWord in collectedWords) {
                                        error("Found $collectedWord twice")
                                    }
                                    collectedWords += collectedWord

                                    val word = wordString.let { Word(it, errors) }
                                    if (word.parts.isEmpty()) {
                                        return@readLines null
                                    }
                                    val translation = parts[1].trim()
                                    val weight: Int? =
                                        parts
                                            .getOrNull(2)
                                            ?.toInt()
                                            ?: word.calcCount(counts)

                                    if (weight == null) {
                                        return@readLines null
                                    }

                                    Triple(word.build(), translation, weight)
                                },
                            ).filterNotNull()
                            .sortedByDescending { it.third }
                    dictionaryName to words
                }

        if (errors.isNotEmpty()) {
            errors.forEach { error(it) }
        }

        File(outputDir, "dictionaries.json").writeText(
            text =
                dictionaries.joinToString(
                    prefix = "[\n",
                    postfix = "\n]",
                    separator = ",\n",
                ) { (name, words) ->
                    val wordsJson: String =
                        words.joinToString(
                            prefix = "[\n",
                            postfix = "\n    ]",
                            separator = ",\n",
                        ) { (word, translation, weight) ->
                            "      {\n        \"word\": \"$word\",\n        \"translation\": \"$translation\",\n        \"weight\": $weight\n      }"
                        }
                    "  {\n    \"name\": \"$name\",\n    \"words\": $wordsJson\n  }"
                },
        )
    }

    private class Counts(
        private val raw: Map<String, Int>,
        private val errors: MutableList<String>? = null,
    ) {
        private val simplified: Map<String, Int> =
            raw
                .map { (word, count) ->
                    withoutNonSpacingMarks(word) to count
                }.associate { it }

        operator fun get(word: String): Int? {
            val letters = word.filter { it.isLetter() }
            return raw[letters]
                ?: simplified[withoutNonSpacingMarks(letters)]
                ?: run {
                    errors?.add("Unknown count of word $word")
                    null
                }
        }

        private fun withoutNonSpacingMarks(string: String): String =
            Normalizer
                .normalize(string, Normalizer.Form.NFD)
                .filter { char -> Character.getType(char) != Character.NON_SPACING_MARK.toInt() }
    }

    private data class Word(
        val parts: List<Part>,
        private val errors: MutableList<String>? = null,
    ) {
        data class Part(
            val value: String,
            val significant: Boolean,
        )

        constructor(
            word: String,
            errors: MutableList<String>? = null,
        ) : this(
            parts =
                word
                    .split(' ')
                    .map(String::trim)
                    .filterNot(String::isEmpty)
                    .map { part ->
                        val additional = part.startsWith('[') && part.endsWith(']')
                        Part(
                            value =
                                when (additional) {
                                    false -> part
                                    true -> part.drop(1).dropLast(1)
                                },
                            significant = !additional,
                        )
                    }.also { parts ->
                        if (!parts.any(Part::significant)) {
                            errors?.add("'$word' has no significant parts")
                        }
                    }.filter(Part::significant),
            errors = errors,
        )

        fun build(): String =
            parts.joinToString(
                separator = " ",
                transform = Part::value,
            )

        fun calcCount(counts: Counts): Int? =
            parts
                .filter(Part::significant)
                .map { it.value.lowercase() }
                .mapNotNull { counts[it] }
                .minOrNull()
    }

    private val dictionaryExtensionSuffix = ".txt"

    private inline fun <R> File.readLines(
        crossinline onError: (line: String, msg: String) -> Unit,
        crossinline buildEntry: (line: String, parts: List<String>) -> R,
    ): Sequence<R> =
        this
            .readLines()
            .asSequence()
            .map(kotlin.String::trim)
            .filterNot { it.isEmpty() || it.startsWith("#") || it.startsWith("//") }
            .mapNotNull { line ->
                try {
                    line
                        .split('|')
                        .map(kotlin.String::trim)
                        .filterNot { it.isEmpty() }
                        .let { parts -> buildEntry(line, parts) }
                } catch (e: Exception) {
                    onError(line, e.message ?: "Unknown error")
                    null
                }
            }
}
