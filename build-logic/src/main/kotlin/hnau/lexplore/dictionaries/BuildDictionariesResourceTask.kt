package hnau.lexplore.dictionaries

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class BuildDictionariesResourceTask : DefaultTask() {

    @get:InputDirectory
    abstract val sourceDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val content = DictionariesResourceGenerator(
            sourceDir = sourceDir.get().asFile,
        ).generate()
        outputDir
            .file("dictionaries.json")
            .get()
            .asFile
            .apply { parentFile.mkdirs() }
            .writeText(content)
    }
}
