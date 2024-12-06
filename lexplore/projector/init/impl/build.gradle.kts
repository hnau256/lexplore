plugins {
    alias(libs.plugins.compose.desktop)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:app"))
            implementation(project(":lexplore:model:init:api"))
            implementation(project(":lexplore:projector:common"))
            implementation(project(":lexplore:projector:init:api"))
        }
    }
}
