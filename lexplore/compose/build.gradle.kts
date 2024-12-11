plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.ksp)
    id("hnau.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":lexplore:projector:common"))
            implementation(project(":lexplore:projector:init:api"))
        }
        commonMain.dependencies {
            implementation(compose.materialIconsExtended)
            implementation(project(":common:app"))
            implementation(project(":common:color"))
            implementation(project(":lexplore:app"))
            implementation(project(":lexplore:model:init:api"))
            implementation(project(":lexplore:projector:common"))
            implementation(project(":lexplore:projector:init:api"))
            implementation(project(":lexplore:projector:init:impl"))
            implementation(project(":lexplore:projector:mainstack:api"))
            implementation(project(":lexplore:projector:mainstack:impl"))
        }
    }
}
