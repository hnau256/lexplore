package hnau.lexplore.dictionaries

import org.gradle.api.Plugin
import org.gradle.api.Project

class DictionariesResourcePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val task = project.tasks.register(
            "buildDictionariesResource",
            BuildDictionariesResourceTask::class.java,
        ) { task ->
            task.sourceDir.set(
                project.rootProject.layout.projectDirectory.dir("data"),
            )
            task.outputDir.set(
                project.layout.projectDirectory.dir("src/main/res/raw"),
            )
        }
        project.tasks
            .matching { it.name == "preBuild" }
            .configureEach { it.dependsOn(task) }
    }
}
