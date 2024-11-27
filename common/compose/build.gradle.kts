plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":common:color"))
        }
    }
}

