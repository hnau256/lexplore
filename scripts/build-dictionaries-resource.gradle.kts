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
        val counts = Counts(
            raw = File(sourceDir, "word_counts.txt")
                .readLines { line, values ->
                    if (values.size != 14) {
                        error("Expcted ID|Word|FREQcount|CD|SUBTLEX_WF|Lg10WF|SUBTLEX_CD|Lg10CD|FREQlow|FREQupper|N|OLD20|Length|SUBTLEX_WF_full, got $line")
                    }
                    val word = values[1].lowercase()
                    val count = values[2].toInt()
                    word to count
                }
                .associate { it }
        )

        val dictionariesDir = File(sourceDir, "dictionaries")
        val collectedWords: MutableSet<String> = mutableSetOf()
        val dictionaries = dictionariesDir
            .list()
            .orEmpty()
            .sorted()
            .map { dictionaryFileName ->
                val dictionaryName = dictionaryFileName.removeSuffix(dictionaryExtensionSuffix)
                if (dictionaryName == dictionaryFileName) {
                    error("Expected file with $dictionaryExtensionSuffix extension, got $dictionaryFileName")
                }
                val words = File(dictionariesDir, dictionaryFileName)
                    .readLines { line, parts ->
                        if (parts.size != 2 && parts.size != 3) {
                            error("Expected <word>|<translation>{|<count>}, got $line")
                        }
                        val wordString = parts[0].trim()

                        val collectedWord = wordString.lowercase()
                        if (collectedWord in collectedWords) {
                            error("Found $collectedWord twice")
                        }
                        collectedWords += collectedWord

                        val word = wordString.let(::Word)
                        val translation = parts[1].trim()
                        val weight = parts
                            .getOrNull(2)
                            ?.toInt()
                            ?: word.calcCount(counts)

                        Triple(word.build(), translation, weight)
                    }
                    .toList()
                    .sortedByDescending(Triple<String, String, Int>::third)
                dictionaryName to words
            }


        File(outputDir, "dictionaries.json").writeText(
            text = dictionaries.joinToString(
                prefix = "[\n",
                postfix = "\n]",
                separator = ",\n",
            ) { (name, words) ->
                val wordsJson: String = words.joinToString(
                    prefix = "[\n",
                    postfix = "\n    ]",
                    separator = ",\n",
                ) { (word, translation, weight) ->
                    "      {\n        \"word\": \"$word\",\n        \"translation\": \"$translation\",\n        \"weight\": $weight\n      }"
                }
                "  {\n    \"name\": \"$name\",\n    \"words\": $wordsJson\n  }"
            }
        )

    }

    private class Counts(
        private val raw: Map<String, Int>,
    ) {

        private val simplified: Map<String, Int> = raw
            .map { (word, count) ->
                withoutNonSpacingMarks(word) to count
            }
            .associate { it }

        operator fun get(
            word: String,
        ): Int {
            val letters = word.filter { it.isLetter() }
            return raw[letters]
                ?: simplified[withoutNonSpacingMarks(letters)]
                ?: error("Unknown count of word $word")
        }

        private fun withoutNonSpacingMarks(
            string: String,
        ): String = Normalizer
            .normalize(string, Normalizer.Form.NFD)
            .filter { char -> Character.getType(char) != Character.NON_SPACING_MARK.toInt() }
    }

    private data class Word(
        val parts: List<Part>,
    ) {

        data class Part(
            val value: String,
            val significant: Boolean,
        )

        constructor(
            word: String,
        ) : this(
            parts = word
                .split(' ')
                .map(String::trim)
                .filterNot(String::isEmpty)
                .map { part ->
                    val additional = part.startsWith('[') && part.endsWith(']')
                    Part(
                        value = when (additional) {
                            false -> part
                            true -> part.drop(1).dropLast(1)
                        },
                        significant = !additional,
                    )
                }
                .takeIf { parts -> parts.any(Part::significant) }
                ?: error("'$word' has no significant parts")
        )

        fun build(): String = parts.joinToString(
            separator = " ",
            transform = Part::value,
        )

        fun calcCount(
            counts: Counts,
        ): Int = parts
            .filter(Part::significant)
            .map { it.value.lowercase() }
            .map { counts[it].toInt() }
            .min()
    }


    private val dictionaryExtensionSuffix = ".txt"

    private inline fun <R> File.readLines(
        crossinline buildEntry: (line: String, parts: List<String>) -> R,
    ): Sequence<R> = this
        .readLines()
        .asSequence()
        .map(kotlin.String::trim)
        .filterNot { it.isEmpty() || it.startsWith("#") || it.startsWith("//") }
        .map { line ->
            line
                .split('|')
                .map(kotlin.String::trim)
                .filterNot { it.isEmpty() }
                .let { parts -> buildEntry(line, parts) }
        }
}