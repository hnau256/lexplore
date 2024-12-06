plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlin.datetime)
            implementation(project(":common:app"))
            implementation(project(":common:color"))
            implementation(project(":lexplore:model:common"))
        }
    }
}
