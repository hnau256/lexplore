package hnau.common.kotlin

import java.io.File
import java.io.InputStreamReader


fun main() {
    val lines = File("dictionary.txt")
        .reader()
        .use(InputStreamReader::readLines)
        .mapNotNull { line ->
            val (greek, russian) = line
                .split('\t')
                .filter(String::isNotEmpty)
                .takeIf { it.size == 2 }
                ?: return@mapNotNull null
            greek.lowercase() to russian.lowercase()
        }
        .distinctBy { (greek) -> greek }

    File("out.txt")
        .writer()
        .use { writer ->
            lines
                .forEach { (greek, russian) ->
                    writer.write("$greek\t$russian")
                    writer.write("\n")
                }
        }
}