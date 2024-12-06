plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":lexplore:projector:common"))
        }
        commonMain.dependencies {
            implementation(project(":lexplore:model:init:api"))
            implementation(project(":lexplore:projector:common"))
        }
    }
}
