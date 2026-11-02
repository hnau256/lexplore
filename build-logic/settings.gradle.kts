pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("org.hnau.plugin.settings") version "1.30.4"
}

rootProject.name = "build-logic"
rootProject.buildFileName = "build-logic.gradle.kts"
