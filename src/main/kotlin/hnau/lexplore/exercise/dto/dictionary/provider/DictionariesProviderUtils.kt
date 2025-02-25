package hnau.lexplore.exercise.dto.dictionary.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.Reader

object DictionariesProviderUtils {

    suspend fun readLines(
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
}