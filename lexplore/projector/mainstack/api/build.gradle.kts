plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lexplore:model:mainstack:api"))
            implementation(project(":lexplore:model:dictionaries:api"))
            implementation(project(":lexplore:projector:common"))
            implementation(project(":lexplore:projector:dictionaries:api"))
        }
    }
}
