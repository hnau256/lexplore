import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import java.io.File
import java.text.Normalizer

tasks.register<BuildDictionariesAssertTask>("buildDictionariesAssert") {
    sourceDir = file("data")
    outputDir = file("src/main/assets")
}

open class BuildDictionariesAssertTask : DefaultTask() {

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
                        val word = parts[0].trim().let(::Word)
                        val translation = parts[1].trim()
                        val count = parts
                            .getOrNull(2)
                            ?.toFloat()
                            ?: word.calcCount(counts)

                        Triple(word.build(), translation, count)
                    }
                    .toList()
                    .sortedByDescending(Triple<String, String, Float>::third)
                dictionaryName to words
            }


        File(outputDir, "dictionaries.json").writeText(
            text = dictionaries.joinToString(
                prefix = "[",
                postfix = "]",
                separator = ",",
            ) { (name, words) ->
                val wordsJson: String = words.joinToString(
                    prefix = "[",
                    postfix = "]",
                    separator = ",",
                ) { (word, translation, count) ->
                    "{\"word\":\"$word\",\"translation\":\"$translation\",\"count\":$count}"
                }
                "{\"name\":\"$name\",\"words\":$wordsJson}"
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
        ): Float = parts
            .filter(Part::significant)
            .map { it.value.lowercase() }
            .map { counts[it].toFloat() }
            .sorted()
            .let { partsCounts ->
                val count = partsCounts.size
                when (count % 2) {
                    0 -> (partsCounts[count / 2 - 1] + partsCounts[count / 2]) / 2
                    else -> partsCounts[count / 2]
                }
            }
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