plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":lexplore:data:api"))
            implementation(project(":lexplore:model:dictionaries:api"))
            implementation(project(":lexplore:projector:common"))
            implementation(project(":lexplore:projector:dictionaries:api"))
        }
    }
}
