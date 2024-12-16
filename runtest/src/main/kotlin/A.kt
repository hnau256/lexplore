package hnau.common.kotlin

import java.io.File
import java.io.InputStreamReader


fun main() {
    val count = File("frequency.txt")
        .reader()
        .use(InputStreamReader::readLines)
        .mapNotNull { line ->
            val (word, countString) = line
                .split(' ')
                .filter(String::isNotEmpty)
                .takeIf { it.size == 2 }
                ?: return@mapNotNull null
            word to countString.toInt()
        }
        .associate { it }
    val dictionary = File("translations.txt")
        .reader()
        .use { reader ->
            reader
                .use(InputStreamReader::readLines)
                .map { line ->
                    val (greek, russian) = line
                        .split('\t')
                        .filter(String::isNotEmpty)
                        .takeIf { it.size == 2 }!!
                    greek to russian
                }
        }
    println(count)
    println(dictionary)
}